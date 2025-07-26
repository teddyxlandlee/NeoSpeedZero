package xland.mcmod.neospeedzero.view;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

import java.util.*;

public record ChallengeSnapshot(UUID recordId, Component title, List<ItemStack> challenges, long[] successTimeMap) implements CustomPacketPayload {
    // The exact time is not used yet. We simply check its non-negativity.
    // Just send to client.

    @ApiStatus.Internal
    public ChallengeSnapshot {
    }

    public static ChallengeSnapshot fromRecord(@NotNull SpeedrunRecord record) {
        return new ChallengeSnapshot(
                record.recordId(),
                record.goal().goal().display().copy(),
                record.challenges().stream().map(SpeedrunChallenge::icon).toList(),
                record.collectedTimes().clone()
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ChallengeSnapshot> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ChallengeSnapshot::recordId,
            ComponentSerialization.STREAM_CODEC, ChallengeSnapshot::title,
            ByteBufCodecs.<RegistryFriendlyByteBuf, ItemStack>list().apply(ItemStack.STREAM_CODEC), ChallengeSnapshot::challenges,
            ByteBufCodecs.LONG_ARRAY, ChallengeSnapshot::successTimeMap,
            ChallengeSnapshot::new
    );

    public void sendToClient(ServerPlayer serverPlayer) {
        NetworkManager.sendToPlayer(serverPlayer, this);
    }

    @Override
    public @NotNull Type<ChallengeSnapshot> type() {
        return ViewPackets.TYPE_SNAPSHOT;
    }

    int totalPageCount() {
        return Math.ceilDivExact(challenges().size(), 63);
    }

    public record Change(UUID recordId, int index, long newValue) implements CustomPacketPayload {
        public static final StreamCodec<RegistryFriendlyByteBuf, Change> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, Change::recordId,
                ByteBufCodecs.VAR_INT, Change::index,
                ByteBufCodecs.LONG, Change::newValue,
                Change::new
        );

        public boolean applyTo(ChallengeSnapshot snapshot) {
            if (!Objects.equals(recordId(), snapshot.recordId())) return false;

            final int index = index();
            if (index >= 0 && index < snapshot.successTimeMap().length) {
                // Can modify
                snapshot.successTimeMap()[index] = newValue();
            }
            return true;
        }

        public void broadcastToAll(PlayerList playerList) {
            NetworkManager.sendToPlayers(playerList.getPlayers(), this);
        }

        @Override
        public @NotNull Type<Change> type() {
            return ViewPackets.TYPE_CHANGE;
        }
    }

    public Component toText() {
        final MutableComponent root = Component.empty();
        root.append(Component.translatable(
                "command.neospeedzero.view_raw.title", title()
        ).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                Component.translatable("message.neospeedzero.record.snapshot.id", String.valueOf(recordId()))
        )))).append("\n");

        final List<ItemStack> yes = new ArrayList<>();
        final List<ItemStack> no = new ArrayList<>();
        {
            int i = 0;
            final long[] timeMap = successTimeMap();
            for (ItemStack challenge : challenges()) {
                List<ItemStack> target = timeMap[i++] >= 0 ? yes : no;
                target.add(challenge);
            }
        }

        // Yes
        root.append(Component.translatable(
                "command.neospeedzero.view_raw.complete.yes", yes.size()
        ).withStyle(ChatFormatting.GREEN)).append("\n");
        root.append(join(yes.iterator()));
        root.append("\n");
        // No
        root.append(Component.translatable(
                "command.neospeedzero.view_raw.complete.no", no.size()
        ).withStyle(ChatFormatting.RED)).append("\n");
        root.append(join(no.iterator()));

        root.append("\n\n").append(Component.translatable("message.neospeedzero.record.snapshot.non-synced"));

        return root;
    }

    private static Component join(Iterator<ItemStack> stacks) {
        final MutableComponent component = Component.empty();
        while (stacks.hasNext()) {
            ItemStack next = stacks.next();
            component.append(next.getDisplayName());
            if (stacks.hasNext()) component.append(", ");
        }
        return component;
    }
}
