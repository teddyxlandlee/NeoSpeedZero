package xland.mcmod.neospeedzero.record.manager;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

@ApiStatus.NonExtendable
public interface NeoSpeedPlayer {
    @Nullable
    //@Deprecated
    default SpeedrunRecord ns0$currentRecord() {
        SpeedrunRecordHolder holder = ns0$serverRecordManager().findRecordByPlayer((ServerPlayer) this);
        return holder == null ? null : holder.record();
    }

    default long ns0$time() {
        //noinspection resource
        return ((ServerPlayer) this).level().getServer().overworld().getGameTime();
    }

    default RecordManager ns0$serverRecordManager() {
        //noinspection resource
        return ((ServerPlayer) this).level().getServer().ns0$recordManager();
    }
}
