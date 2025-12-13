package xland.mcmod.neospeedzero.mixin;

import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;

@Mixin(InventoryChangeTrigger.class)
abstract class InventoryChangeTriggerMixin {
    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V")
    private void onInventoryChange(ServerPlayer serverPlayer, Inventory inventory, ItemStack itemStack, CallbackInfo ci) {
        NeoSpeedLifecycle.onInventoryChange(serverPlayer, itemStack);
    }
}
