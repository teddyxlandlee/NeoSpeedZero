package xland.mcmod.neospeedzero.record;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.NonExtendable
public interface NeoSpeedPlayer {
    @Nullable
    SpeedrunRecord ns0$currentRecord();

    void ns0$setCurrentRecord(@Nullable SpeedrunRecord record);

    String PERSISTENT_DATA_KEY = "neospeedzero:speedrun_record";

    default long ns0$time() {
        return Objects.requireNonNull(((ServerPlayer) this).getServer()).overworld().getGameTime();
    }
}
