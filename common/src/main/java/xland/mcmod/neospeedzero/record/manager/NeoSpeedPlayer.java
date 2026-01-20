package xland.mcmod.neospeedzero.record.manager;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

@ApiStatus.NonExtendable
@ApiStatus.Obsolete
@FunctionalInterface
public interface NeoSpeedPlayer {
    @Nullable
    //@Deprecated
    default SpeedrunRecord getCurrentRecord() {
        SpeedrunRecordHolder holder = getServerRecordManager().findRecordByPlayer(ns0$self());
        return holder == null ? null : holder.record();
    }

    default long getTime() {
        //noinspection resource
        return ns0$self().level().getServer().overworld().getGameTime();
    }

    default RecordManager getServerRecordManager() {
        //noinspection resource
        return NeoSpeedServer.getRecordManager(ns0$self().level().getServer());
    }

    static NeoSpeedPlayer of(ServerPlayer player) {
        return () -> player;
    }

    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    ServerPlayer ns0$self();
}
