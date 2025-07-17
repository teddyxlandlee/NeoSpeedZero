package xland.mcmod.neospeedzero.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xland.mcmod.neospeedzero.itemext.ItemExtensions;

@Mixin(FireworkRocketItem.class)
abstract class FireworkRocketItemMixin {
    // Return `true` to KEEP, `false` to SKIP

    @WrapWithCondition(method = "useOn", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
    ))
    private boolean stopShrinking(ItemStack instance, int decrement) {
        return !ItemExtensions.isInfiniteFirework(instance);
    }

    @WrapWithCondition(method = "use", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"
    ))
    private boolean stopConsuming(ItemStack instance, int amount, LivingEntity entity) {
        return !ItemExtensions.isInfiniteFirework(instance);
    }
}
