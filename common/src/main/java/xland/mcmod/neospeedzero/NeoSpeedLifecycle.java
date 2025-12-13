package xland.mcmod.neospeedzero;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.api.NeoSpeedLifecycleEvents;
import xland.mcmod.neospeedzero.api.SpeedrunStartupConfig;
import xland.mcmod.neospeedzero.command.RecordReference;
import xland.mcmod.neospeedzero.itemext.ItemExtensions;
import xland.mcmod.neospeedzero.mixin.PlayerAdvancementsAccessor;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.record.manager.PlayerRole;
import xland.mcmod.neospeedzero.record.manager.RecordManager;
import xland.mcmod.neospeedzero.record.manager.SpeedrunRecordHolder;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;
import xland.mcmod.neospeedzero.util.event.ActionResult;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.view.ChallengeSnapshot;

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

        ActionResult actionResult = NeoSpeedLifecycleEvents.START_RECORD.invoker().onStart(player, startupConfig);
        if (!actionResult.getResult(true)) {    // interruptFalse
            // Do not start
            return Optional.of(Component.translatable("message.neospeedzero.record.start.cancel"));
        }
        record = startupConfig.createRecord(player.ns0$time());
        Component message = player.ns0$serverRecordManager().startHosting(record, player);
        if (message != null) return Optional.of(message);

        NeoSpeedMessages.announceRecordStart(player, record);
        initSpeedrunBeginning(player, record);

        return Optional.empty();
    }

    private static void initSpeedrunBeginning(ServerPlayer player, SpeedrunRecord record) {
        record.difficulty().onStart(player, record);

        // Check existing items & advancements
        checkExistingThings(player);
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

        if (player.ns0$serverRecordManager().getPlayerRole(previousRecord.recordId(), player) != PlayerRole.HOST) {
            return Optional.of(Component.translatable("message.neospeedzero.stop.no_host"));
        }

        ActionResult actionResult = NeoSpeedLifecycleEvents.FORCE_STOP_RECORD.invoker().onStop(player);
        if (!actionResult.getResult(true)) {  // interruptFalse
            // Do not stop
            return Optional.of(Component.translatable("message.neospeedzero.record.stop.force.cancel"));
        }

        SpeedrunRecordHolder prevHolder = player.ns0$serverRecordManager().endRecord(previousRecord.recordId());
        if (prevHolder == null) {
            return Optional.of(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
        }

        NeoSpeedMessages.announceRecordForceStop(player, previousRecord);
        return Optional.empty();
    }

    public static Optional<Component> quitSpeedrun(ServerPlayer player) {
        SpeedrunRecordHolder holder = player.ns0$serverRecordManager().leaveRecord(player);
        if (holder != null) {
            // was running
            NeoSpeedMessages.announceRecordQuit(player, holder.record());
            return Optional.empty();
        } else {
            return Optional.of(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
        }
    }

    public static Optional<Component> joinSpeedrun(ServerPlayer player, RecordReference ref) {
        SpeedrunRecord record = player.ns0$currentRecord();
        if (record != null) {
            return Optional.of(Component.translatable(
                    "message.neospeedzero.record.start.started",
                    player.getDisplayName(),
                    record.snapshot()
            ));
        }

        RecordManager manager = player.ns0$serverRecordManager();
        return ref.parse(manager).map(
                uuid -> {
                    Component msg = manager.joinRecord(uuid, player);
                    if (msg == null) {
                        SpeedrunRecordHolder holder = manager.findRecordByUuid(uuid);
                        if (holder == null) {
                            return Optional.of(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
                        }

                        NeoSpeedMessages.announceRecordJoin(player, holder.record());
                        initSpeedrunBeginning(player, holder.record());
                    }
                    return Optional.ofNullable(msg);
                },
                Optional::of
        );
    }

    public static void viewRecord(ServerPlayer audience, @NotNull SpeedrunRecord record) {
        // TODO: permission check
        ChallengeSnapshot.fromRecord(record).sendToClient(audience);
    }

    public static void viewRecordRaw(ServerPlayer audience, @NotNull SpeedrunRecord record) {
        audience.sendSystemMessage(ChallengeSnapshot.fromRecord(record).toText());
    }

    public static void viewRecordDialog(ServerPlayer audience, @NotNull SpeedrunRecord record) {
        audience.openDialog(Holder.direct(record.asDialog()));
    }

    public static void listDialog(ServerPlayer audience) {
        audience.openDialog(Holder.direct(SpeedrunGoal.Holder.toDialog()));
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

    // will be called by the mixin
    public static void onAdvancementMade(ServerPlayer player, AdvancementHolder advancement) {
        SpeedrunRecord record = player.ns0$currentRecord();
        if (record == null) return;

        for (int i = 0, size = record.challenges().size(); i < size; i++) {
            if (record.collectedTimes()[i] >= 0) continue;
            SpeedrunChallenge challenge = record.challenges().get(i);

            final int idx = i;
            challenge.challenge().ifRight(advancementKey -> {
                if (advancement.id().equals(advancementKey.identifier())) {
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
        //noinspection resource
        new ChallengeSnapshot.Change(record.recordId(), index, currentTime).broadcastToAll(serverPlayer.level().getServer().getPlayerList());

        NeoSpeedMessages.announceChallengeComplete(serverPlayer, record, index, currentTime);
        NeoSpeedLifecycleEvents.COMPLETE_SINGLE_CHALLENGE.invoker().onComplete(serverPlayer, record, index);

        if (record.shallComplete()) {
            completeRecord(serverPlayer, record);
        }
    }

    static void completeRecord(ServerPlayer serverPlayer, SpeedrunRecord record) {
        final long currentTime = serverPlayer.ns0$time();
        record.finishTime().setValue(currentTime);

        serverPlayer.ns0$serverRecordManager().endRecord(record.recordId());
        NeoSpeedMessages.announceRecordComplete(serverPlayer, record, currentTime);
        NeoSpeedLifecycleEvents.COMPLETE_RECORD.invoker().onComplete(serverPlayer, record);
    }

    public static void register() {
        // Server lifecycle events
        PlatformEvents.getInstance().whenServerStarting(server -> server.ns0$recordManager().loadFromServer());
        PlatformEvents.getInstance().whenServerStopped(server -> {
            server.ns0$recordManager().saveToServer();
            // Then, remove cached holders
            LOGGER.info("Clearing SpeedRunGoal.Holder");
            SpeedrunGoal.Holder.clearHolders();
        });

        // Prevent irrelevant players from obtaining marked items
        PlatformEvents.getInstance().preServerPlayerTick(serverPlayer -> {
            final @Nullable UUID uuid = serverPlayer.ns0$serverRecordManager().findRecordIdByPlayer(serverPlayer);

            serverPlayer.getInventory().forEach(stack -> {
                if (stack.isEmpty()) return;
                if (ItemExtensions.matchesRecordId(stack, uuid)) {
                    return;     // This is OK
                }
                // Not matched: remove it
                stack.setCount(0);
            });
        });
    }

    private static final Logger LOGGER = LogUtils.getLogger();
}
