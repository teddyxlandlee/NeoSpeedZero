package xland.mcmod.neospeedzero.view;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

public interface ViewPackets {
    ResourceLocation ID_SNAPSHOT = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "view_challenge");
    CustomPacketPayload.Type<ChallengeSnapshot> TYPE_SNAPSHOT = new CustomPacketPayload.Type<>(ID_SNAPSHOT);

    ResourceLocation ID_CHANGE = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "sync_challenge");
    CustomPacketPayload.Type<ChallengeSnapshot.Change> TYPE_CHANGE = new CustomPacketPayload.Type<>(ID_CHANGE);

    ResourceLocation ID_C2S_REQUEST = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "request_view_challenge");
    CustomPacketPayload.Type<Request> TYPE_C2S_REQUEST = new CustomPacketPayload.Type<>(ID_C2S_REQUEST);

    static void register() {
        NetworkManager.registerS2CPayloadType(TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC);

        NetworkManager.registerReceiver(NetworkManager.c2s(), TYPE_C2S_REQUEST, Request.STREAM_CODEC, (singleton, context) -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                SpeedrunRecord record = serverPlayer.ns0$currentRecord();
                if (record == null) return;
                NeoSpeedLifecycle.viewRecord(serverPlayer, record);
            }
        });
    }

    class Request implements CustomPacketPayload {
        private Request() {}
        public static final Request INSTANCE = new Request();

        public static final StreamCodec<RegistryFriendlyByteBuf, Request> STREAM_CODEC = StreamCodec.unit(Request.INSTANCE);

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE_C2S_REQUEST;
        }

        @Override
        public String toString() {
            return "ViewPackets.Request";
        }
    }
}
