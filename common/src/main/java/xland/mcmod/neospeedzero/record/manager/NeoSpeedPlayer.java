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

    @SuppressWarnings("resource")
    static MinecraftServer getServer(ServerPlayer player) {
        return player.level().getServer();
    }
}
