package xland.mcmod.neospeedzero.neoforge.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class PlatformEventsNeoForge extends PlatformEvents {
    private PlatformEventsNeoForge() {}
    private static final PlatformEventsNeoForge INSTANCE = new PlatformEventsNeoForge();
    public static PlatformEventsNeoForge getInstance() { return INSTANCE; }

    public void whenServerStarting(Consumer<MinecraftServer> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, event -> callback.accept(event.getServer()));
    }

    public void whenServerStopped(Consumer<MinecraftServer> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> callback.accept(event.getServer()));
    }

    public void prePlayerTick(Consumer<Player> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Pre.class, event -> callback.accept(event.getEntity()));
    }

    public void registerCommand(Supplier<LiteralArgumentBuilder<CommandSourceStack>> nodeBuilder) {
        Objects.requireNonNull(nodeBuilder, "nodeBuilder cannot be null");
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            // In NeoSpeedZero, we only need the dispatcher
            event.getDispatcher().register(nodeBuilder.get());
        });
    }

    public void registerResourceReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        NeoForge.EVENT_BUS.addListener(
                AddServerReloadListenersEvent.class,
                event -> event.addListener(id, factory.apply(event.getServerResources().getRegistryLookup()))
        );
    }

    @ApiStatus.Internal
    public static final DeferredRegister<@NotNull GameRule<?>> GAME_RULE_REG = DeferredRegister.create(Registries.GAME_RULE, NeoSpeedZero.MOD_ID);

    @Override
    public Predicate<? super MinecraftServer> registerBooleanGameRule(String id, GameRuleCategory category, boolean defaultValue) {
        var gameRuleProvider = GAME_RULE_REG.register(id, () -> new GameRule<@NotNull Boolean>(
                category, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, BooleanUtils::toInteger, defaultValue, FeatureFlagSet.of()
        ));
        return server -> server.getGameRules().get(gameRuleProvider.value());
    }

//    @OnlyIn(Dist.CLIENT)
    public void registerKeyMapping(Supplier<KeyMapping> keyMappingSupplier) {
        final KeyMapping keyMapping = Objects.requireNonNull(keyMappingSupplier.get(), "keyMapping cannot be null.");
        IEventBus bus = ModList.get().getModContainerById(NeoSpeedZero.MOD_ID).map(ModContainer::getEventBus).orElseThrow();
        bus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(keyMapping));
    }
    
//    @OnlyIn(Dist.CLIENT)
    public void postClientTick(Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, _ -> callback.run());
    }
}
