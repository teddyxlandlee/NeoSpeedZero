package xland.mcmod.neospeedzero.util;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.DurationFormatUtils;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

public class TimeUtil {
    public static final long TICK_TO_NANOS = 50000000L;

    public static Component duration(SpeedrunRecord record, long currentTime) {
        long millis = record.duration(currentTime).toMillis();
        // We simply use HMS format now
        // TODO: i18n format
        return Component.literal(millis >= 0 ? DurationFormatUtils.formatDurationHMS(millis) : "???");
    }
}
