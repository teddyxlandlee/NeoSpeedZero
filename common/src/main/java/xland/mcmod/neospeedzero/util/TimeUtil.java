package xland.mcmod.neospeedzero.util;

import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.DurationFormatUtils;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

import java.time.Duration;

public final class TimeUtil {
    public static final long TICK_TO_NANOS = 50000000L;
    static final String PLACEHOLDER_KEY = "message.neospeedzero.duration.placeholder2";
    // TODO: Make all translatable strings fallback-friendly, to get prepared for Paper port

    public static Component duration(SpeedrunRecord record, long currentTime) {
        Duration duration = record.duration(currentTime);
        long millis = duration.toMillis();
        // We simply use HMS format now
        // TODO: i18n format
        if (millis < 0) {
            return Component.literal("???");
        }
        String formattedDuration = DurationFormatUtils.formatDurationHMS(millis);
        return Component.translatableWithFallback(
                PLACEHOLDER_KEY,    // "%s"
                /*fallback=*/formattedDuration,         // only used in LangPatch
                /*args=*/formattedDuration
        );
    }

    private TimeUtil() {}
}
