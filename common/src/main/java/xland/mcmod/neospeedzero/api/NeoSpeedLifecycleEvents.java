package xland.mcmod.neospeedzero.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

public interface NeoSpeedLifecycleEvents {
    Event<CompleteSingleChallenge> COMPLETE_SINGLE_CHALLENGE = EventFactory.createLoop();
    Event<CompleteRecord> COMPLETE_RECORD = EventFactory.createLoop();
    Event<StartRecord> START_RECORD = EventFactory.createEventResult();
    Event<ForceStopRecord> FORCE_STOP_RECORD = EventFactory.createEventResult();

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
        EventResult onStart(ServerPlayer player, SpeedrunStartupConfig startupConfig);
    }

    @FunctionalInterface
    interface ForceStopRecord {
        EventResult onStop(ServerPlayer player);
    }
}
