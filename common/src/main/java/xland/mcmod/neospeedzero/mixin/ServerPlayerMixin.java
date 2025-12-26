package xland.mcmod.neospeedzero.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedPlayer;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedServer;

import java.util.Optional;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin implements NeoSpeedPlayer {
    @Inject(
            method = "readAdditionalSaveData",
            at = @At("RETURN")
    )
    private void onReadData(ValueInput input, CallbackInfo ci) {
        Optional<SpeedrunRecord> optionalLegacyRecord = input.read("neospeedzero:speedrun_record", SpeedrunRecord.CODEC);
        if (optionalLegacyRecord.isPresent()) {
            @SuppressWarnings("resource")
            MinecraftServer server = ((ServerPlayer)(Object)this).level().getServer();
            NeoSpeedServer.of(server).ns0$recordManager().registerLegacyRecord(this, optionalLegacyRecord.get());
        }
    }
}
