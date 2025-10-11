package xland.mcmod.neospeedzero.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;

@Mixin(PlayerAdvancements.class)
abstract class PlayerAdvancementsMixin {
    @Accessor("player")
    abstract ServerPlayer ns0$player();

    @Inject(
            method = "award",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onComplete(AdvancementHolder advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        NeoSpeedLifecycle.onAdvancementMade(ns0$player(), advancement);
    }
}
