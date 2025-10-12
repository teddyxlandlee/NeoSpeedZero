package xland.mcmod.neospeedzero.util.network.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.util.network.ServerToClientPayload;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public final class PlatformNetworkImpl {
    private PlatformNetworkImpl() {}

    public static <P extends CustomPacketPayload> void registerC2SImpl(
            CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, P> typeAndCodec,
            Consumer<ServerPlayer> callback
    ) {
        Objects.requireNonNull(typeAndCodec, "typeAndCodec cannot be null.");
        Objects.requireNonNull(callback, "callback cannot be null.");
        registerPayloadHandlers(event -> {
            // According to architectury
            event.registrar(typeAndCodec.type().id().getNamespace()).optional().playToServer(
                    typeAndCodec.type(), typeAndCodec.codec(),
                    (payload, context) -> callback.accept((ServerPlayer) context.player())
            );
        });
    }

    public static <C extends ServerToClientPayload> void registerS2CImpl(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, C> typeAndCodec) {
        Objects.requireNonNull(typeAndCodec, "typeAndCodec cannot be null.");
        registerPayloadHandlers(event -> {
            // According to architectury, as well
            PayloadRegistrar registrar = event.registrar(typeAndCodec.type().id().getNamespace()).optional();
            if (FMLEnvironment.getDist() != Dist.CLIENT) {
                registrar.playToClient(typeAndCodec.type(), typeAndCodec.codec());
            } else {
                registrar.playToClient(typeAndCodec.type(), typeAndCodec.codec(), (payload, context) -> payload.onClientReceive());
            }
        });
    }

    private static void registerPayloadHandlers(Consumer<RegisterPayloadHandlersEvent> consumer) {
        ModList.get().getModContainerById(NeoSpeedZero.MOD_ID).map(ModContainer::getEventBus).orElseThrow().addListener(RegisterPayloadHandlersEvent.class, consumer);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    public static void sendToPlayer(ServerToClientPayload payload, ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, payload);
    }

    public static void sendToPlayers(ServerToClientPayload payload, Collection<? extends ServerPlayer> players) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
        players.forEach(p -> p.connection.send(packet));
    }
}
