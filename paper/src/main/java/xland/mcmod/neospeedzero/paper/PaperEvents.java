package xland.mcmod.neospeedzero.paper;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.apache.commons.lang3.Validate;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;
import xland.mcmod.neospeedzero.util.event.Event;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PaperEvents extends PlatformEvents {
    private PaperEvents() {}
    private static final PaperEvents INSTANCE = new PaperEvents();
    public static PaperEvents getInstance() { return INSTANCE; }

    private static final ComponentLogger LOGGER = ComponentLogger.logger();

    private static <T> Function<? super Iterable<? extends Consumer<T>>, ? extends Consumer<T>> consumerInvoker() {
        return l -> t -> {
            for (var x: l) x.accept(t);
        };
    }

    static final Event<Consumer<MinecraftServer>, Consumer<MinecraftServer>> SERVER_STARTING = Event.of(consumerInvoker());
    static final Event<Consumer<MinecraftServer>, Consumer<MinecraftServer>> SERVER_STOPPING = Event.of(consumerInvoker());

    @Override
    public void whenServerStarting(Consumer<MinecraftServer> callback) {
        SERVER_STARTING.register(callback);
    }


    @Override
    public void whenServerStopped(Consumer<MinecraftServer> callback) {
        SERVER_STOPPING.register(callback);
    }

    static final Event<Consumer<Player>, Consumer<Player>> PRE_PLAYER_TICK = Event.of(consumerInvoker());

    @Override
    protected void prePlayerTick(Consumer<Player> callback) {
        PRE_PLAYER_TICK.register(callback);
    }

    static Consumer<org.bukkit.entity.Player> getPlayerTickTask() {
        final var invoker = PRE_PLAYER_TICK.invoker();
        return p -> invoker.accept(CraftBukkitReflections.asVanillaPlayer(p));
    }

    static final Event<
            Supplier<? extends LiteralArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack>>,
            Iterable<? extends Supplier<? extends LiteralArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack>>>> COMMANDS =
            Event.of(Function.identity());

    @Override
    public void registerCommand(Supplier<LiteralArgumentBuilder<CommandSourceStack>> nodeBuilder) {
        Validate.validState(io.papermc.paper.command.brigadier.CommandSourceStack.class.isAssignableFrom(CommandSourceStack.class));
        @SuppressWarnings("unchecked")
        Supplier<? extends LiteralArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack>> s = (Supplier<? extends LiteralArgumentBuilder<io.papermc.paper.command.brigadier.CommandSourceStack>>) nodeBuilder;
        COMMANDS.register(s);
    }

    private static final AtomicReference<SpeedrunGoalManager> GOAL_MANAGER = new AtomicReference<>();

    static void applyGoals(Map<Identifier, SpeedrunGoal> map) {
        final var goalManager = GOAL_MANAGER.get();
        Objects.requireNonNull(goalManager, "goalManager cannot be null");
        goalManager.applyMap(map);
    }

    static void applyGoalsFrom(MinecraftServer server) {
        final var prefix = SpeedrunGoalManager.GOAL_KEY_ID.getNamespace() + '/' + SpeedrunGoalManager.GOAL_KEY_ID.getPath() + '/';
        final FileToIdConverter converter = FileToIdConverter.json(prefix);

        final var jsonOps = RegistryOps.create(JsonOps.INSTANCE, CraftBukkitReflections.getRegistryAccess());

        final HashMap<Identifier, SpeedrunGoal> result = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(server.getResourceManager(), converter, jsonOps, SpeedrunGoal.CODEC, result);

        applyGoals(result);
    }

    @Override
    public void registerResourceReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        final var registryAccess = CraftBukkitReflections.getRegistryAccess();
        PreparableReloadListener listener = factory.apply(registryAccess);
        if (listener instanceof SpeedrunGoalManager goalManager) {
            Object prev;
            if ((prev = GOAL_MANAGER.getAndSet(goalManager)) != null) {
                LOGGER.error("Duplicate SpeedrunGoalManager: {}", prev);
            }
        } else {
            LOGGER.warn("Unrecognizable reload listener: {}. Skipped.", listener);
        }
    }

    @Override
    public Predicate<? super MinecraftServer> registerBooleanGameRule(String id, GameRuleCategory category, boolean defaultValue) {
        // TODO: provide alternative options for game rule
        return com.google.common.base.Predicates.alwaysTrue();
    }

    @Deprecated
    @Override
    public void registerKeyMapping(Supplier<net.minecraft.client.KeyMapping> keyMapping) {
        throw new UnsupportedOperationException("Wrong side");
    }

    @Deprecated
    @Override
    public void postClientTick(Runnable callback) {
        throw new UnsupportedOperationException("Wrong side");
    }
}
