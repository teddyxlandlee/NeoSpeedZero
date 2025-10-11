package xland.mcmod.neospeedzero.util.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public final class PlatformEvents {
    @ExpectPlatform
    public static void whenServerStarting(Consumer<MinecraftServer> callback) {
        throw expectPlatform(callback);
    }

    @ExpectPlatform
    public static void whenServerStopped(Consumer<MinecraftServer> callback) {
        throw expectPlatform(callback);
    }

    public static void preServerPlayerTick(Consumer<ServerPlayer> callback) {
        prePlayerTick(player -> {
            if (player instanceof ServerPlayer) {
                callback.accept((ServerPlayer) player);
            }
        });
    }

    @ExpectPlatform
    private static void prePlayerTick(Consumer<Player> callback) {
        throw expectPlatform(callback);
    }

    @ExpectPlatform
    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        throw expectPlatform(callback);
    }

    @ExpectPlatform
    public static void registerResourceReloadListener(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        throw expectPlatform(id, factory);
    }

    @ExpectPlatform
    public static <T extends GameRules.Value<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type) {
        throw expectPlatform(name, category, type);
    }

    @Environment(EnvType.CLIENT)
    @ExpectPlatform
    public static void registerKeyMapping(KeyMapping keyMapping) {
        throw expectPlatform(keyMapping);
    }

    @Environment(EnvType.CLIENT)
    @ExpectPlatform
    public static void postClientTick(Runnable callback) {
        throw expectPlatform(callback);
    }

    private PlatformEvents() {}

    private static Error expectPlatform(Object... ignore) {
        // pass in ignored vararg in case unused
        return new AssertionError("ExpectPlatform");
    }

}
