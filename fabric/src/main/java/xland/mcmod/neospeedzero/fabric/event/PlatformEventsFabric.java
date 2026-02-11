package xland.mcmod.neospeedzero.fabric.event;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.util.event.Event;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PlatformEventsFabric extends PlatformEvents {
    private PlatformEventsFabric() {}
    private static final PlatformEventsFabric INSTANCE = new PlatformEventsFabric();
    public static PlatformEventsFabric getInstance() { return INSTANCE; }

    @Override
    public void whenServerStarting(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STARTING.register(callback::accept);
    }

    public void whenServerStopped(Consumer<MinecraftServer> callback) {
        ServerLifecycleEvents.SERVER_STOPPED.register(callback::accept);
    }

    public static final Event<Consumer<Player>, Consumer<Player>> EVENT_PRE_PLAYER_TICK = Event.of(
            l -> player -> {
                for (var e: l) e.accept(player);
            }
    );

    public void prePlayerTick(Consumer<Player> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        EVENT_PRE_PLAYER_TICK.register(callback);
    }

    public void registerCommand(Supplier<LiteralArgumentBuilder<CommandSourceStack>> nodeBuilder) {
        Objects.requireNonNull(nodeBuilder, "nodeBuilder cannot be null.");
        CommandRegistrationCallback.EVENT.register((commandDispatcher, _, _) -> {
            // In NeoSpeedZero, we only need the dispatcher
            commandDispatcher.register(nodeBuilder.get());
        });
    }

    public void registerResourceReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
                Objects.requireNonNull(id, "id cannot be null."),
                new RegistryResourceReloadListener(factory)
        );
    }

    @Override
    public Predicate<? super MinecraftServer> registerBooleanGameRule(String id, GameRuleCategory category, boolean defaultValue) {
        var gameRule = GameRuleBuilder.forBoolean(defaultValue).category(category).buildAndRegister(Identifier.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, id));
        return server -> server.getGameRules().get(gameRule);
    }

    @Environment(EnvType.CLIENT)
    public void registerKeyMapping(Supplier<KeyMapping> keyMapping) {
        KeyMappingHelper.registerKeyMapping(keyMapping.get());
    }

    @Environment(EnvType.CLIENT)
    public void postClientTick(Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        ClientTickEvents.END_CLIENT_TICK.register(_ -> callback.run());
    }
}
