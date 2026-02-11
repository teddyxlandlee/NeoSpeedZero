package xland.mcmod.neospeedzero;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.function.Predicate;

public final class NeoSpeedGameRules {
    private static final Predicate<? super MinecraftServer> PRE_announceSpeedruns;

    static {
        PRE_announceSpeedruns = PlatformEvents.getInstance().registerBooleanGameRule("announce_speedruns", GameRuleCategory.CHAT, true);
    }

    public static boolean announcesSpeedruns(MinecraftServer server) {
        return PRE_announceSpeedruns.test(server);
    }

    public static final Logger LOGGER = LogUtils.getLogger();

    private NeoSpeedGameRules() {}
}
