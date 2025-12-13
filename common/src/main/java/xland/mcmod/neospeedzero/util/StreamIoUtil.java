package xland.mcmod.neospeedzero.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;

public final class StreamIoUtil {
    private StreamIoUtil() {}
    private static final Gson GSON = new Gson();

    @org.jspecify.annotations.NullMarked
    public static <B extends ByteBuf, K, V> StreamCodec<B, Map<K, V>> ofMap(StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec) {
        return StreamCodec.of(
                /*encode=*/(buf, map) -> {
                    VarInt.write(buf, map.size());
                    map.forEach((k, v) -> {
                        keyCodec.encode(buf, k);
                        valueCodec.encode(buf, v);
                    });
                },
                /*decode=*/buf -> {
                    final int size = VarInt.read(buf);
                    LinkedHashMap<K, V> map = LinkedHashMap.newLinkedHashMap(size);
                    for (int i = 0; i < size; i++) {
                        map.put(keyCodec.decode(buf), valueCodec.decode(buf));
                    }
                    return map;
                }
        );
    }

    private static final StreamCodec<@NotNull ByteBuf, @NotNull String> INF_UTF8 = StreamCodec.of(
            /*encode=*/(buf, s) -> {
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                VarInt.write(buf, bytes.length);
                buf.writeBytes(bytes);
            },
            /*decode=*/buf -> {
                int length = VarInt.read(buf);
                return buf.readBytes(length).toString(StandardCharsets.UTF_8);
            }
    );

    @org.jspecify.annotations.NullMarked
    public static <O, C extends Collection<O>> StreamCodec<RegistryFriendlyByteBuf, C> ofJsonArrayString(Codec<O> codec, IntFunction<C> collectionFactory) {
        return StreamCodec.of(
                /*encode=*/(buf, objs) -> {
                    RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, buf.registryAccess());
                    var arr = new JsonArray();
                    for (O obj : objs) {
                        JsonElement json = codec.encodeStart(ops, obj).getOrThrow();
                        arr.add(json);
                    }
                    INF_UTF8.encode(buf, arr.toString());
                },
                /*decode=*/buf -> {
                    JsonArray json = GSON.fromJson(INF_UTF8.decode(buf), JsonArray.class);
                    C c = collectionFactory.apply(json.size());
                    RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, buf.registryAccess());
                    for (JsonElement e : json) {
                        c.add(codec.parse(ops, e).getOrThrow());
                    }
                    return c;
                }
        );
    }

    public static RegistryFriendlyByteBuf byteBuf(byte @Nullable [] buf, RegistryAccess registryAccess) {
        return new RegistryFriendlyByteBuf(
                buf != null ? Unpooled.wrappedBuffer(buf) : Unpooled.buffer(),
                registryAccess
        );
    }
}
