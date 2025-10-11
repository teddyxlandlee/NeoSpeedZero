package xland.mcmod.neospeedzero.api;

import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.util.event.ActionResult;
import xland.mcmod.neospeedzero.util.event.Event;

public interface NeoSpeedLifecycleEvents {
    Event<CompleteSingleChallenge, CompleteSingleChallenge> COMPLETE_SINGLE_CHALLENGE = Event.of(l -> (serverPlayer, record, index) -> {
        for (var e: l) e.onComplete(serverPlayer, record, index);
    });
    Event<CompleteRecord, CompleteRecord> COMPLETE_RECORD = Event.of(l -> (serverPlayer, record) -> {
        for (var e: l) e.onComplete(serverPlayer, record);
    });
    Event<StartRecord, StartRecord> START_RECORD = Event.of(l -> (player, startupConfig) -> {
        for (var e : l) {
            var result = e.onStart(player, startupConfig);
            if (result.interrupts()) return result;
        }
        return ActionResult.pass();
    });
    Event<ForceStopRecord, ForceStopRecord> FORCE_STOP_RECORD = Event.of(l -> player -> {
        for (var e : l) {
            var result = e.onStop(player);
            if (result.interrupts()) return result;
        }
        return ActionResult.pass();
    });

    @FunctionalInterface
    interface CompleteSingleChallenge {
        void onComplete(ServerPlayer serverPlayer, SpeedrunRecord record, int index);
    }

    @FunctionalInterface
    interface CompleteRecord {
        void onComplete(ServerPlayer serverPlayer, SpeedrunRecord record);
    }

    @FunctionalInterface
    interface StartRecord {
        ActionResult onStart(ServerPlayer player, SpeedrunStartupConfig startupConfig);
    }

    @FunctionalInterface
    interface ForceStopRecord {
        ActionResult onStop(ServerPlayer player);
    }
}
