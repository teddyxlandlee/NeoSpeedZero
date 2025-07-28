package xland.mcmod.neospeedzero;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.api.NeoSpeedLifecycleEvents;
import xland.mcmod.neospeedzero.api.SpeedrunStartupConfig;
import xland.mcmod.neospeedzero.itemext.ItemExtensions;
import xland.mcmod.neospeedzero.mixin.PlayerAdvancementsAccessor;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.view.ChallengeSnapshot;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NeoSpeedLifecycle {
    private NeoSpeedLifecycle() {}

    public static Optional<Component> startSpeedrun(ServerPlayer player, SpeedrunStartupConfig startupConfig) {
        SpeedrunRecord record = player.ns0$currentRecord();
        if (record != null) {
            return Optional.of(Component.translatable(
                    "message.neospeedzero.record.start.started",
                    player.getDisplayName(),
                    record.snapshot()
            ));
        }

        EventResult eventResult = NeoSpeedLifecycleEvents.START_RECORD.invoker().onStart(player, startupConfig);
        if (eventResult == EventResult.interruptFalse()) {
            // Do not start
            return Optional.of(Component.translatable("message.neospeedzero.record.start.cancel"));
        }
        record = startupConfig.createRecord(player.ns0$time());
        player.ns0$setCurrentRecord(record);
        NeoSpeedMessages.announceRecordStart(player, record);

        record.difficulty().onStart(player, record);

        // Check existing items & advancements
        checkExistingThings(player);

        return Optional.empty();
    }

    private static void checkExistingThings(ServerPlayer player) {
        // Items
        player.getInventory().forEach(itemStack -> onInventoryChange(player, itemStack));
        // Advancement
        ((PlayerAdvancementsAccessor) player.getAdvancements()).ns0$progress().forEach(((advancementHolder, advancementProgress) -> {
            if (!advancementProgress.isDone()) return;
            onAdvancementMade(player, advancementHolder);
        }));
    }

    public static Optional<Component> stopSpeedrun(ServerPlayer player) {
        SpeedrunRecord previousRecord = player.ns0$currentRecord();
        if (previousRecord == null) {
            return Optional.of(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
        }

        EventResult eventResult = NeoSpeedLifecycleEvents.FORCE_STOP_RECORD.invoker().onStop(player);
        if (eventResult == EventResult.interruptFalse()) {
            // Do not stop
            return Optional.of(Component.translatable("message.neospeedzero.record.stop.force.cancel"));
        }

        player.ns0$setCurrentRecord(null);
        NeoSpeedMessages.announceRecordForceStop(player, previousRecord);
        return Optional.empty();
    }

    public static void viewRecord(ServerPlayer audience, @NotNull SpeedrunRecord record) {
        // TODO: permission check
        ChallengeSnapshot.fromRecord(record).sendToClient(audience);
    }

    public static void viewRecordRaw(ServerPlayer audience, @NotNull SpeedrunRecord record) {
        audience.sendSystemMessage(ChallengeSnapshot.fromRecord(record).toText());
    }

    public static void onInventoryChange(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || ItemExtensions.isModGivenItem(stack)) return;

        SpeedrunRecord record = player.ns0$currentRecord();
        if (record == null) return;

        for (int i = 0, size = record.challenges().size(); i < size; i++) {
            if (record.collectedTimes()[i] >= 0) continue;
            SpeedrunChallenge challenge = record.challenges().get(i);

            final int idx = i;
            challenge.challenge().ifLeft(itemPredicate -> {
                if (itemPredicate.test(stack)) {
                    // Gets the item
                    onCompleteSingleChallenge(player, record, idx);
                }
            });
        }
    }

    private static void onAdvancementMade(ServerPlayer player, AdvancementHolder advancement) {
        SpeedrunRecord record = player.ns0$currentRecord();
        if (record == null) return;

        for (int i = 0, size = record.challenges().size(); i < size; i++) {
            if (record.collectedTimes()[i] >= 0) continue;
            SpeedrunChallenge challenge = record.challenges().get(i);

            final int idx = i;
            challenge.challenge().ifRight(advancementKey -> {
                if (advancement.id().equals(advancementKey.location())) {
                    // Matched advancement
                    onCompleteSingleChallenge(player, record, idx);
                }
            });
        }
    }

    public static void onCompleteSingleChallenge(ServerPlayer serverPlayer, SpeedrunRecord record, int index) {
        final long currentTime = serverPlayer.ns0$time();
        record.markComplete(index, currentTime);

        // sync to client
        new ChallengeSnapshot.Change(record.recordId(), index, currentTime).broadcastToAll(Objects.requireNonNull(serverPlayer.getServer()).getPlayerList());

        NeoSpeedMessages.announceChallengeComplete(serverPlayer, record, index, currentTime);
        NeoSpeedLifecycleEvents.COMPLETE_SINGLE_CHALLENGE.invoker().onComplete(serverPlayer, record, index);

        if (record.shallComplete()) {
            completeRecord(serverPlayer, record);
        }
    }

    static void completeRecord(ServerPlayer serverPlayer, SpeedrunRecord record) {
        final long currentTime = serverPlayer.ns0$time();
        record.finishTime().setValue(currentTime);

        serverPlayer.ns0$setCurrentRecord(null);
        NeoSpeedMessages.announceRecordComplete(serverPlayer, record, currentTime);
        NeoSpeedLifecycleEvents.COMPLETE_RECORD.invoker().onComplete(serverPlayer, record);
    }

    public static void register() {
        PlayerEvent.PLAYER_ADVANCEMENT.register(NeoSpeedLifecycle::onAdvancementMade);
        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) -> {
            // Move SpeedrunRecord
            newPlayer.ns0$setCurrentRecord(oldPlayer.ns0$currentRecord());
        });

        // Prevent irrelevant players from obtaining marked items
        TickEvent.PLAYER_PRE.register(player -> {
            if (!(player instanceof ServerPlayer)) return;
            SpeedrunRecord record = ((ServerPlayer) player).ns0$currentRecord();
            UUID uuid = record == null ? null : record.recordId();

            player.getInventory().forEach(stack -> {
                if (stack.isEmpty()) return;
                if (ItemExtensions.matchesRecordId(stack, uuid)) {
                    return;     // This is OK
                }
                // Not matched: remove it
                stack.setCount(0);
            });
        });
    }
}
