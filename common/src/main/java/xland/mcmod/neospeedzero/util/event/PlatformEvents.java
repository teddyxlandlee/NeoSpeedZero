package xland.mcmod.neospeedzero.util.event;

import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.util.PlatformDependent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class PlatformEvents {
    public static PlatformEvents getInstance() {
        class Holder {
            static final PlatformEvents INSTANCE = PlatformDependent.Platform.probe(PlatformEvents.class);
        }
        return Holder.INSTANCE;
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

    public abstract Supplier<GameRule<@NotNull Boolean>> registerBooleanGameRule(String id, GameRuleCategory category, boolean defaultValue);

    @Environment(EnvType.CLIENT)
    public abstract void registerKeyMapping(KeyMapping keyMapping);

    @Environment(EnvType.CLIENT)
    public abstract void postClientTick(Runnable callback);
}
