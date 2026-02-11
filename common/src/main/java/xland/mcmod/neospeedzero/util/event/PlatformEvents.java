package xland.mcmod.neospeedzero.util.event;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jetbrains.annotations.ApiStatus;
import xland.mcmod.neospeedzero.util.PlatformAPI;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class PlatformEvents {
    public static PlatformEvents getInstance() {
        return PlatformAPI.getInstance().events();
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

    public abstract void registerCommand(Supplier<LiteralArgumentBuilder<CommandSourceStack>> nodeBuilder);

    public abstract void registerResourceReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory);

    public abstract Predicate<? super MinecraftServer> registerBooleanGameRule(String id, GameRuleCategory category, boolean defaultValue);

    @Environment(EnvType.CLIENT)
    // use Supplier to avoid potential classloading issue
    public abstract void registerKeyMapping(Supplier<KeyMapping> keyMapping);

    @Environment(EnvType.CLIENT)
    public abstract void postClientTick(Runnable callback);
}
