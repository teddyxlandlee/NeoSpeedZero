package xland.mcmod.neospeedzero.util.access;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public interface AdvancementProgressGetter {
    Map<AdvancementHolder, AdvancementProgress> ns0$progress();
}
