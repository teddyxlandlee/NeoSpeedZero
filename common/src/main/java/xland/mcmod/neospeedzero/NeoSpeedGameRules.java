package xland.mcmod.neospeedzero;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

public class NeoSpeedGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> ANNOUNCE_SPEEDRUNS;

    static {
        ANNOUNCE_SPEEDRUNS = PlatformEvents.registerGameRule("announceSpeedruns", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
    }

    public static final Logger LOGGER = LogUtils.getLogger();
}
