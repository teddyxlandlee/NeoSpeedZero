package xland.mcmod.neospeedzero.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xland.mcmod.neospeedzero.record.NeoSpeedPlayer;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin implements NeoSpeedPlayer {
    @Unique
    private SpeedrunRecord ns0$currentRecord;

    @Override
    public @Nullable SpeedrunRecord ns0$currentRecord() {
        return ns0$currentRecord;
    }

    @Override
    public void ns0$setCurrentRecord(@Nullable SpeedrunRecord record) {
        this.ns0$currentRecord = record;
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("RETURN")
    )
    private void onReadData(ValueInput input, CallbackInfo ci) {
        this.ns0$setCurrentRecord(input.read(PERSISTENT_DATA_KEY, SpeedrunRecord.CODEC).orElse(null));
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("RETURN")
    )
    private void onWriteData(ValueOutput output, CallbackInfo ci) {
        output.storeNullable(PERSISTENT_DATA_KEY, SpeedrunRecord.CODEC, this.ns0$currentRecord());
    }
}
