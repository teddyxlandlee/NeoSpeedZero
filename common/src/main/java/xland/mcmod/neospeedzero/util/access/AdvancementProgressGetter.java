package xland.mcmod.neospeedzero.util.access;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;

import java.util.Map;

public interface AdvancementProgressGetter {
    Map<AdvancementHolder, AdvancementProgress> ns0$progress();
}
