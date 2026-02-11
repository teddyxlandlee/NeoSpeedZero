package xland.mcmod.neospeedzero.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xland.mcmod.neospeedzero.util.access.AdvancementProgressGetter;

import java.util.Map;

@Mixin(PlayerAdvancements.class)
interface PlayerAdvancementsAccessor extends AdvancementProgressGetter {
    @Accessor("progress")
    @Override
    Map<AdvancementHolder, AdvancementProgress> ns0$progress();
}
