package xland.mcmod.neospeedzero.util.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public abstract class PlatformEvents {
    @ExpectPlatform
    public static PlatformEvents getInstance() {
        throw new AssertionError("ExpectPlatform");
    }

    public abstract void whenServerStarting(Consumer<MinecraftServer> callback);

    public abstract void whenServerStopped(Consumer<MinecraftServer> callback);

    public void preServerPlayerTick(Consumer<ServerPlayer> callback) {
        prePlayerTick(player -> {
            if (player instanceof ServerPlayer) {
                callback.accept((ServerPlayer) player);
            }
        });
    }

    protected abstract void prePlayerTick(Consumer<Player> callback);

    public abstract void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback);

    public abstract void registerResourceReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory);

    public abstract  <T extends GameRules.Value<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Category category, GameRules.Type<T> type);

    @Environment(EnvType.CLIENT)
    public abstract void registerKeyMapping(KeyMapping keyMapping);

    @Environment(EnvType.CLIENT)
    public abstract void postClientTick(Runnable callback);
}
