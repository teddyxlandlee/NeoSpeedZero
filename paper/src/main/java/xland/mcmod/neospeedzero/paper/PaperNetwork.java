package xland.mcmod.neospeedzero.paper;

import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;
import xland.mcmod.neospeedzero.util.network.ServerToClientPayload;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@NullMarked
public final class PaperNetwork extends PlatformNetwork {
    private PaperNetwork() {}
    private static final PaperNetwork INSTANCE = new PaperNetwork();
    public static PaperNetwork getInstance() { return INSTANCE; }

    static final ConcurrentMap<Key, Consumer<Player>> C2S = new ConcurrentHashMap<>();

    @Override
    protected <P extends CustomPacketPayload> void registerC2SImpl(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, P> typeAndCodec, Consumer<ServerPlayer> callback) {
        Objects.requireNonNull(callback, "callback cannot be null.");
        // Does not care the content of payload.
        Key key = CraftBukkitReflections.asKey(typeAndCodec.type().id());
        C2S.put(key, p -> callback.accept(CraftBukkitReflections.asVanillaServerPlayer(p)));
    }

    private static final ConcurrentMap<Identifier, StreamCodec<RegistryFriendlyByteBuf, ?>> S2C = new ConcurrentHashMap<>();

    static Collection<?> getS2CKeys() {
        return S2C.keySet();
    }

    @Override
    protected <C extends ServerToClientPayload> void registerS2CImpl(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, C> typeAndCodec) {
        Objects.requireNonNull(typeAndCodec.type(), "typeAndCodec.type() cannot be null.");
        Objects.requireNonNull(typeAndCodec.codec(), "typeAndCodec.codec() cannot be null.");
        S2C.put(typeAndCodec.type().id(), typeAndCodec.codec());
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        throw new UnsupportedOperationException("Wrong side");
    }

    private static byte @Nullable[] payloadToBytes(ServerToClientPayload payload) {
        @SuppressWarnings("unchecked")
        StreamCodec<RegistryFriendlyByteBuf, ServerToClientPayload> codec = (StreamCodec<RegistryFriendlyByteBuf, ServerToClientPayload>) S2C.get(payload.type().id());
        if (codec == null) return null;

        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftBukkitReflections.getRegistryAccess());
        try {
            codec.encode(buf, payload);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    @Override
    public void sendToPlayer(ServerToClientPayload payload, ServerPlayer serverPlayer) {
        Player bukkitPlayer = CraftBukkitReflections.asBukkitPlayer(serverPlayer);
        byte[] payloadBytes = payloadToBytes(payload);
        if (payloadBytes == null) return;   // unknown packet

        bukkitPlayer.sendPluginMessage(NeoSpeedZeroPaper.getInstance(), payload.type().id().toString(), payloadBytes);
    }

    @Override
    public void sendToPlayers(ServerToClientPayload payload, Collection<? extends ServerPlayer> players) {
        byte[] payloadBytes = payloadToBytes(payload);
        if (payloadBytes == null) return;   // unknown packet

        String channel = payload.type().id().toString();
        var plugin = NeoSpeedZeroPaper.getInstance();

        for (ServerPlayer player : players) {
            CraftBukkitReflections.asBukkitPlayer(player).sendPluginMessage(plugin, channel, payloadBytes);
        }
    }
}
