package xland.mcmod.neospeedzero;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.ApiStatus;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.util.TimeUtil;

public final class NeoSpeedMessages {
    private NeoSpeedMessages() {}

    private static void announce(ServerPlayer serverPlayer, Component component) {
        @SuppressWarnings("resource")
        final MinecraftServer server = serverPlayer.level().getServer();
        final boolean announceSpeedruns = NeoSpeedGameRules.announcesSpeedruns(server);

        if (announceSpeedruns) {
            server.getPlayerList().broadcastSystemMessage(component, false);
        } else {
            serverPlayer.sendSystemMessage(component);
        }
    }

    static void announceRecordStart(ServerPlayer serverPlayer, SpeedrunRecord record) {
        announce(serverPlayer, NeoSpeedTranslations.RECORD_START.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot()));
    }

    static void announceRecordForceStop(ServerPlayer serverPlayer, SpeedrunRecord record) {
        announce(serverPlayer, NeoSpeedTranslations.RECORD_STOP_FORCE.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot()));
    }

    static void announceRecordQuit(ServerPlayer serverPlayer, SpeedrunRecord record) {
        announce(serverPlayer, NeoSpeedTranslations.PLAYER_JOIN_QUITTING.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot()));
    }

    static void announceRecordJoin(ServerPlayer serverPlayer, SpeedrunRecord record) {
        announce(serverPlayer, NeoSpeedTranslations.PLAYER_JOIN_SUCCESS.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot()));
    }

    static void announceChallengeComplete(ServerPlayer serverPlayer, SpeedrunRecord record, int index, long currentTime) {
        SpeedrunChallenge challenge = record.challenges().get(index);

        @SuppressWarnings("resource")
        final MinecraftServer server = serverPlayer.level().getServer();
        final boolean announceSpeedruns = NeoSpeedGameRules.announcesSpeedruns(server);

        final Component component = NeoSpeedTranslations.CHALLENGE_COMPLETE.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot(),
                challenge.icon().getDisplayName(),  // already wrapped
                progress(record),
                TimeUtil.duration(record, currentTime));

        if (announceSpeedruns) {
            server.getPlayerList().broadcastSystemMessage(component, false);
            server.getPlayerList().getPlayers().forEach(p -> playSound(p, false));
        } else {
            serverPlayer.sendSystemMessage(component);
            playSound(serverPlayer, false);
        }
    }

    static void announceRecordComplete(ServerPlayer serverPlayer, SpeedrunRecord record, long currentTime) {
        @SuppressWarnings("resource")
        final MinecraftServer server = serverPlayer.level().getServer();
        final boolean announceSpeedruns = NeoSpeedGameRules.announcesSpeedruns(server);

        final Component component = NeoSpeedTranslations.RECORD_COMPLETE.createWithArgs(serverPlayer.getDisplayName(),
                record.snapshot(),
                TimeUtil.duration(record, currentTime));

        if (announceSpeedruns) {
            server.getPlayerList().broadcastSystemMessage(component, false);
            server.getPlayerList().getPlayers().forEach(p -> playSound(p, p.equals(serverPlayer)));
        } else {
            serverPlayer.sendSystemMessage(component);
            playSound(serverPlayer, true);
        }
    }

    private static void playSound(ServerPlayer serverPlayer, boolean myWin) {
        final SoundEvent soundEvent = myWin ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.EXPERIENCE_ORB_PICKUP;
        playNotifySound(serverPlayer, soundEvent);
    }

    private static void playNotifySound(ServerPlayer player, SoundEvent soundEvent) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent),
                SoundSource.MASTER,
                player.getX(), player.getY(), player.getZ(),
                /*volume=*/.8F, /*pitch=*/1.0F,
                /*seed=*/player.getRandom().nextLong()
        ));
    }

    @ApiStatus.Internal
    public static Component snapshotFor(SpeedrunRecord record, boolean brackets) {
        MutableComponent component = Component.empty()
                .append(record.goal().display().copy())
                .append(Component.literal("#" + record.recordId().toString().substring(0, 4)).withStyle(ChatFormatting.GRAY));
        if (brackets) component = ComponentUtils.wrapInSquareBrackets(component);
        return component.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(snapshotHover(record))));
    }
    
    private static Component snapshotHover(SpeedrunRecord record) {
        MutableComponent t = Component.empty();
        t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_GOAL_ID.createWithArgs(String.valueOf(record.goal().id())))
                .append("\n");
        t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_DIFFICULTY.createWithArgs(record.difficulty().displayedName()))
                .append("\n");
        if (record.hasCompleted()) {
            t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_PROGRESS.createWithArgs(progress(record).withStyle(ChatFormatting.GREEN))).append("\n");
            t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_FINISH_TIME.createWithArgs(TimeUtil.duration(record, record.finishTime().longValue()))).append("\n");
        } else {
            t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_PROGRESS.createWithArgs(progress(record).withStyle(ChatFormatting.RED))).append("\n");
        }
        t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_ID.createWithArgs(String.valueOf(record.recordId()))).append("\n");
        t.append(NeoSpeedTranslations.RECORD_SNAPSHOT_NON_SYNCED.create().withStyle(ChatFormatting.GRAY));
        return t;
    }

    private static MutableComponent progress(SpeedrunRecord record) {
        return NeoSpeedTranslations.RECORD_SNAPSHOT_PROGRESS_DATA.createWithArgs(record.completedCount(),
                record.totalCount());
    }
}
