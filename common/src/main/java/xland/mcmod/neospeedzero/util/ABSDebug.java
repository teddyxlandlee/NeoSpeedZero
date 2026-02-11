package xland.mcmod.neospeedzero.util;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.function.Consumer;

@ApiStatus.Internal
public final class ABSDebug {
    private ABSDebug() {}
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEBUG = Integer.getInteger("ns0.debug", 0);

    public static boolean enabled(int x) {
        return DEBUG != 0 && ((1 << x) & DEBUG) != 0;
    }

    public static void debug(int x, Consumer<Logger> log) {
        if (enabled(x)) log.accept(LOGGER);
    }
}
