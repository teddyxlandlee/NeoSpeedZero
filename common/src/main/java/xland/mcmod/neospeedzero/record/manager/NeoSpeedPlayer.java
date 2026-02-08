package xland.mcmod.neospeedzero.record.manager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

@ApiStatus.NonExtendable
public interface NeoSpeedPlayer {
    static @Nullable SpeedrunRecord getCurrentRecord(ServerPlayer player) {
        SpeedrunRecordHolder holder = getServerRecordManager(player).findRecordByPlayer(player);
        return holder == null ? null : holder.record();
    }

    static RecordManager getServerRecordManager(ServerPlayer player) {
        return NeoSpeedServer.getRecordManager(getServer(player));
    }

    static long getTime(ServerPlayer player) {
        //noinspection resource
        return getServer(player).overworld().getGameTime();
    }

    @Nullable
    @Deprecated
    default SpeedrunRecord getCurrentRecord() {
        return getCurrentRecord(ns0$self());
    }

    @Deprecated
    default long getTime() {
        return getTime(ns0$self());
    }

    @Deprecated
    default RecordManager getServerRecordManager() {
        return getServerRecordManager(ns0$self());
    }

    @Deprecated
    static NeoSpeedPlayer of(ServerPlayer player) {
        return () -> player;
    }

    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    @Deprecated
    ServerPlayer ns0$self();

    @SuppressWarnings("resource")
    static MinecraftServer getServer(ServerPlayer player) {
        return player.level().getServer();
    }
}
