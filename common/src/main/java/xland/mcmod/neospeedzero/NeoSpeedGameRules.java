package xland.mcmod.neospeedzero;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.function.Supplier;

public final class NeoSpeedGameRules {

    public static final Supplier<GameRule<@NotNull Boolean>> ANNOUNCE_SPEEDRUNS;

    static {
        ANNOUNCE_SPEEDRUNS = PlatformEvents.getInstance().registerBooleanGameRule("announce_speedruns", GameRuleCategory.CHAT, true);
    }

    public static final Logger LOGGER = LogUtils.getLogger();

    private NeoSpeedGameRules() {}
}
