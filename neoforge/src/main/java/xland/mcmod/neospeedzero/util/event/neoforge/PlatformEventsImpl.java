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
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlatformEventsImpl extends PlatformEvents {
    private PlatformEventsImpl() {}
    private static final PlatformEventsImpl INSTANCE = new PlatformEventsImpl();

    @SuppressWarnings("unused")
    public static PlatformEvents getInstance() {
        return INSTANCE;
    }

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

    public void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            // In NeoSpeedZero, we only need the dispatcher
            callback.accept(event.getDispatcher());
        });
    }

    public void registerResourceReloadListener(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        NeoForge.EVENT_BUS.addListener(
                AddServerReloadListenersEvent.class,
                event -> event.addListener(id, factory.apply(event.getServerResources().getRegistryLookup()))
        );
    }

    public <T extends GameRules.Value<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRules.register(name, category, type);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void registerKeyMapping(KeyMapping keyMapping) {
        Objects.requireNonNull(keyMapping, "keyMapping cannot be null.");
        IEventBus bus = ModList.get().getModContainerById(NeoSpeedZero.MOD_ID).map(ModContainer::getEventBus).orElseThrow();
        bus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(keyMapping));
    }
    
    @OnlyIn(Dist.CLIENT)
    public void postClientTick(Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> callback.run());
    }
}
