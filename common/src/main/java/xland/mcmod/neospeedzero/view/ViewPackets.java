package xland.mcmod.neospeedzero.view;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedPlayer;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

public interface ViewPackets {
    Identifier ID_SNAPSHOT = Identifier.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "view_challenge");
    CustomPacketPayload.Type<@NotNull ChallengeSnapshot> TYPE_SNAPSHOT = new CustomPacketPayload.Type<>(ID_SNAPSHOT);

    Identifier ID_CHANGE = Identifier.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "sync_challenge");
    CustomPacketPayload.Type<ChallengeSnapshot.@NotNull Change> TYPE_CHANGE = new CustomPacketPayload.Type<>(ID_CHANGE);

    Identifier ID_C2S_REQUEST = Identifier.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "request_view_challenge");
    CustomPacketPayload.Type<@NotNull Request> TYPE_C2S_REQUEST = new CustomPacketPayload.Type<>(ID_C2S_REQUEST);

    static void register() {
        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registering (1/2)"));
        // ClientBound
        PlatformNetwork.getInstance().registerS2C(new CustomPacketPayload.TypeAndCodec<>(TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC));
        PlatformNetwork.getInstance().registerS2C(new CustomPacketPayload.TypeAndCodec<>(TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC));

        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registered (2/2)"));

        // ServerBound
        PlatformNetwork.getInstance().registerC2S(
                new CustomPacketPayload.TypeAndCodec<>(TYPE_C2S_REQUEST, Request.STREAM_CODEC),
                serverPlayer -> {
                    SpeedrunRecord record = NeoSpeedPlayer.of(serverPlayer).getCurrentRecord();
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

        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull Request> STREAM_CODEC = StreamCodec.unit(Request.INSTANCE);

        @Override
        public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
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
