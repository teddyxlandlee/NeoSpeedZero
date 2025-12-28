package xland.mcmod.neospeedzero.util.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.util.PlatformAPI;

import java.util.Collection;
import java.util.function.Consumer;

@org.jspecify.annotations.NullMarked
public abstract class PlatformNetwork {
    public static PlatformNetwork getInstance() {
        return PlatformAPI.getInstance().network();
    }

    public <P extends CustomPacketPayload> void registerC2S(
            CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, P> typeAndCodec,
            Consumer<ServerPlayer> callback
    ) {
        registerC2SImpl(transform(typeAndCodec), callback);
    }

    public <C extends ServerToClientPayload> void registerS2C(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, C> typeAndCodec) {
        registerS2CImpl(transform(typeAndCodec));
    }

    protected abstract <P extends CustomPacketPayload> void registerC2SImpl(
            CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, P> typeAndCodec,
            Consumer<ServerPlayer> callback
    );

    protected abstract <C extends ServerToClientPayload> void registerS2CImpl(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, C> typeAndCodec);

    @Environment(EnvType.CLIENT)
    public abstract void sendToServer(CustomPacketPayload payload);

    public abstract void sendToPlayer(ServerToClientPayload payload, ServerPlayer serverPlayer);

    public abstract void sendToPlayers(ServerToClientPayload payload, Collection<? extends ServerPlayer> players);
    /**
     * Since architectury used to wrap a ByteArray around the real data, we have to implement it as well
     * for compatibility (with older versions of mod, plugins, etc.)
     */
    static <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, T> transform(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, T> original) {
        return new CustomPacketPayload.TypeAndCodec<>(original.type(), new TransformedStreamCodec<>(original.codec()));
    }

    private record TransformedStreamCodec<T extends CustomPacketPayload>(StreamCodec<RegistryFriendlyByteBuf, T> original) implements StreamCodec<RegistryFriendlyByteBuf, T> {
        private static void packByteArray(ByteBuf dest, ByteBuf src) {
            VarInt.write(dest, src.readableBytes());  // length
            dest.writeBytes(src);
        }

        private static void unpackByteArray(ByteBuf dest, ByteBuf src) {
            int length = VarInt.read(src);
            dest.writeBytes(src, length);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, T payload) {
            ByteBuf localBufCache = BUF_CACHE.get();
            try {
                original.encode(new RegistryFriendlyByteBuf(localBufCache, buf.registryAccess()), payload);
                packByteArray(buf, localBufCache);
            } finally {
                localBufCache.clear();
            }
        }

        @Override
        public T decode(RegistryFriendlyByteBuf buf) {
            ByteBuf localBufCache = BUF_CACHE.get();
            try {
                unpackByteArray(localBufCache, buf);
                return original.decode(new RegistryFriendlyByteBuf(localBufCache, buf.registryAccess()));
            } finally {
                localBufCache.clear();
            }
        }

        private static final ThreadLocal<ByteBuf> BUF_CACHE = ThreadLocal.withInitial(Unpooled::buffer);
    }
}
