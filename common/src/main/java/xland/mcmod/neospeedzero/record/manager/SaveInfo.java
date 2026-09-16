package xland.mcmod.neospeedzero.record.manager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SaveInfo(boolean onServerSave, long timestamp) {
    public SaveInfo(boolean onServerSave) {
        this(onServerSave, System.currentTimeMillis());
    }

    public static final StreamCodec<ByteBuf, SaveInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SaveInfo::onServerSave,
            ByteBufCodecs.LONG, SaveInfo::timestamp,
            SaveInfo::new
    );
}
