package xland.mcmod.neospeedzero.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xland.mcmod.neospeedzero.util.access.CustomDataTagProvider;

// Since copyTag() consumes unnecessary resources, and we just
// read it without modification, getting it unsafely is reasonable.
@Mixin(CustomData.class)
interface CustomDataAccessor extends CustomDataTagProvider {
    @Accessor("tag")
    @Override
    CompoundTag ns0$getUnsafe();
}
