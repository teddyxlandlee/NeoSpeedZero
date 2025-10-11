package xland.mcmod.neospeedzero.util.network.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.util.network.ServerToClientPayload;

import java.util.Collection;
import java.util.function.Consumer;

public final class PlatformNetworkImpl {
    private PlatformNetworkImpl() {}

    public static <P extends CustomPacketPayload> void registerC2SImpl(
            CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, P> typeAndCodec,
            Consumer<ServerPlayer> callback
    ) {
        PayloadTypeRegistry.playC2S().register(typeAndCodec.type(), typeAndCodec.codec());
        ServerPlayNetworking.registerGlobalReceiver(
                typeAndCodec.type(), (payload, context) -> callback.accept(context.player())
        );
    }

    public static <C extends ServerToClientPayload> void registerS2CImpl(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, C> typeAndCodec) {
        PayloadTypeRegistry.playS2C().register(typeAndCodec.type(), typeAndCodec.codec());
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerS2CReceiver(typeAndCodec);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void registerS2CReceiver(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, ? extends ServerToClientPayload> typeAndCodec) {
        ClientPlayNetworking.registerGlobalReceiver(
                typeAndCodec.type(),
                (payload, context) -> payload.onClientReceive()
        );
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void sendToPlayer(ServerToClientPayload payload, ServerPlayer serverPlayer) {
        ServerPlayNetworking.send(serverPlayer, payload);
    }

    public static void sendToPlayers(ServerToClientPayload payload, Collection<? extends ServerPlayer> players) {
        Packet<ClientCommonPacketListener> packet = ServerPlayNetworking.createS2CPacket(payload);
        players.forEach(p -> p.connection.send(packet));
    }
}
