package xland.mcmod.neospeedzero.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$2")
// private final ContainerListener containerListener = new ContainerListener() {...};
abstract class ServerPlayerContainerListenerMixin {
    @Accessor("field_29183")    // this$0
    abstract ServerPlayer ns0$player();

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/InventoryChangeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V"
    ), method = "slotChanged")
    private void onSlotChange(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack, CallbackInfo ci) {
        NeoSpeedLifecycle.onInventoryChange(ns0$player(), stack);
    }
}
