package xland.mcmod.neospeedzero.view;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registering (1/2)"));
        // ClientBound
        class ClientBoundMethods {
            static void snapshot(ChallengeSnapshot snapshot) {
                Minecraft.getInstance().setScreen(new ViewChallengeScreen(snapshot));
            }

            static void change(ChallengeSnapshot.Change change) {
                if (Minecraft.getInstance().screen instanceof ViewChallengeScreen viewChallengeScreen) {
                    viewChallengeScreen.onDataUpdate(change);
                }
            }
        }

        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(
                    NetworkManager.s2c(), ViewPackets.TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC,
                    (snapshot, context) -> context.queue(() -> ClientBoundMethods.snapshot(snapshot))
            );
            NetworkManager.registerReceiver(
                    NetworkManager.s2c(), ViewPackets.TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC,
                    (change, context) -> ClientBoundMethods.change(change)
            );
        } else {
            NetworkManager.registerS2CPayloadType(ViewPackets.TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ViewPackets.TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC);
        }
        xland.mcmod.neospeedzero.util.ABSDebug.debug(4, l -> l.info("ViewPackets registered (2/2)"));

        // ServerBound
        NetworkManager.registerReceiver(NetworkManager.c2s(), TYPE_C2S_REQUEST, Request.STREAM_CODEC, (singleton, context) -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                SpeedrunRecord record = serverPlayer.ns0$currentRecord();
                if (record != null) {
                    NeoSpeedLifecycle.viewRecord(serverPlayer, record);
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable(
                            "message.neospeedzero.record.stop.absent", serverPlayer.getDisplayName()
                    ));
                }
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
