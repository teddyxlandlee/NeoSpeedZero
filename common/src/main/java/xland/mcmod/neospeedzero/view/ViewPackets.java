package xland.mcmod.neospeedzero.view;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

public interface ViewPackets {
    ResourceLocation ID_SNAPSHOT = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "view_challenge");
    CustomPacketPayload.Type<ChallengeSnapshot> TYPE_SNAPSHOT = new CustomPacketPayload.Type<>(ID_SNAPSHOT);

    ResourceLocation ID_CHANGE = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "sync_challenge");
    CustomPacketPayload.Type<ChallengeSnapshot.Change> TYPE_CHANGE = new CustomPacketPayload.Type<>(ID_CHANGE);

    ResourceLocation ID_C2S_REQUEST = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "request_view_challenge");
    CustomPacketPayload.Type<Request> TYPE_C2S_REQUEST = new CustomPacketPayload.Type<>(ID_C2S_REQUEST);

    static void register() {
        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registering (1/2)"));
        // ClientBound
        PlatformNetwork.registerS2C(new CustomPacketPayload.TypeAndCodec<>(TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC));
        PlatformNetwork.registerS2C(new CustomPacketPayload.TypeAndCodec<>(TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC));

        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registered (2/2)"));

        // ServerBound
        PlatformNetwork.registerC2S(
                new CustomPacketPayload.TypeAndCodec<>(TYPE_C2S_REQUEST, Request.STREAM_CODEC),
                serverPlayer -> {
                    SpeedrunRecord record = serverPlayer.ns0$currentRecord();
                    if (record != null) {
                        NeoSpeedLifecycle.viewRecord(serverPlayer, record);
                    } else {
                        serverPlayer.sendSystemMessage(Component.translatable(
                                "message.neospeedzero.record.stop.absent", serverPlayer.getDisplayName()
                        ));
                    }
                }
        );
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

        @Override
        public int hashCode() {
            return System.identityHashCode(INSTANCE);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Request;
        }
    }
}
