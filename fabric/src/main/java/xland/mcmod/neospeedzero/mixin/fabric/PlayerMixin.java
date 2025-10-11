package xland.mcmod.neospeedzero.mixin.fabric;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xland.mcmod.neospeedzero.util.event.fabric.PlatformEventsImpl;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    private void onTick(CallbackInfo ci) {
        PlatformEventsImpl.EVENT_PRE_PLAYER_TICK.invoker().accept((Player)(Object) this);
    }
}
