package xland.mcmod.neospeedzero.util;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.DurationFormatUtils;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

public final class TimeUtil {
    public static final long TICK_TO_NANOS = 50000000L;
    static final String PLACEHOLDER_KEY = "message.neospeedzero.duration.placeholder";

    public static Component duration(SpeedrunRecord record, long currentTime) {
        long millis = record.duration(currentTime).toMillis();
        // We simply use HMS format now
        // TODO: i18n format
        if (millis < 0) {
            return Component.literal("???");
        }
//        return Component.literal(DurationFormatUtils.formatDurationHMS(millis));
        return Component.translatableWithFallback(
                PLACEHOLDER_KEY,    // "%s"
                /*fallback=*/Long.toUnsignedString(millis),         // only used in LangPatch
                /*args=*/DurationFormatUtils.formatDurationHMS(millis)
        );
    }

    private TimeUtil() {}
}
