package xland.mcmod.neospeedzero.util.event.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import xland.mcmod.neospeedzero.util.event.Event;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PlatformEventsImpl extends PlatformEvents {
    private PlatformEventsImpl() {}
    private static final PlatformEventsImpl INSTANCE = new PlatformEventsImpl();

    @SuppressWarnings("unused")
    public static PlatformEvents getInstance() {    // ACTUAL
        return INSTANCE;
    }

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

    public void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            // In NeoSpeedZero, we only need the dispatcher
            callback.accept(commandDispatcher);
        });
    }

    public void registerResourceReloadListener(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(
                Objects.requireNonNull(id, "id cannot be null."),
                new RegistryResourceReloadListener(factory)
        );
    }

    public <T extends GameRules.Value<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type) {
        return GameRuleRegistry.register(name, category, type);
    }

    @Environment(EnvType.CLIENT)
    public void registerKeyMapping(KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }

    @Environment(EnvType.CLIENT)
    public void postClientTick(Runnable callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        ClientTickEvents.END_CLIENT_TICK.register(client -> callback.run());
    }
}
