package xland.mcmod.neospeedzero.util.event.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlatformEventsImpl {
    private PlatformEventsImpl() {}

    public static void whenServerStarting(Consumer<MinecraftServer> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, event -> callback.accept(event.getServer()));
    }

    public static void whenServerStopped(Consumer<MinecraftServer> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> callback.accept(event.getServer()));
    }

    public static void prePlayerTick(Consumer<Player> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Pre.class, event -> callback.accept(event.getEntity()));
    }

    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            // In NeoSpeedZero, we only need the dispatcher
            callback.accept(event.getDispatcher());
        });
    }

    public static void registerResourceReloadListener(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        NeoForge.EVENT_BUS.addListener(
                AddServerReloadListenersEvent.class,
                event -> event.addListener(id, factory.apply(event.getRegistryAccess()))
        );
    }

    public static <T extends GameRules.Value<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRules.register(name, category, type);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void registerKeyMapping(KeyMapping keyMapping) {
        Objects.requireNonNull(keyMapping, "keyMapping cannot be null.");
        NeoForge.EVENT_BUS.addListener(RegisterKeyMappingsEvent.class, event -> event.register(keyMapping));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void postClientTick(Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
//        ClientTickEvents.END_CLIENT_TICK.register(client -> callback.run());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> callback.run());
    }
}
