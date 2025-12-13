package xland.mcmod.neospeedzero.record.manager;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record SpeedrunPlayerInfo(UUID host, Set<UUID> participants) {
    public static final StreamCodec<@NotNull ByteBuf, @NotNull SpeedrunPlayerInfo> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SpeedrunPlayerInfo::host,
            ByteBufCodecs.collection(LinkedHashSet::new, UUIDUtil.STREAM_CODEC), SpeedrunPlayerInfo::participants,
            SpeedrunPlayerInfo::new
    );
    public static final MapCodec<SpeedrunPlayerInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("host").forGetter(SpeedrunPlayerInfo::host),
                    UUIDUtil.CODEC_LINKED_SET.fieldOf("participants").forGetter(SpeedrunPlayerInfo::participants)
            ).apply(instance, SpeedrunPlayerInfo::new)
    );

    public SpeedrunPlayerInfo(UUID host) {
        this(host, new LinkedHashSet<>());
    }

    @Contract(pure = true)
    public @UnmodifiableView Set<UUID> getAllPlayers() {
        return ImmutableSet.<UUID>builderWithExpectedSize(participants().size() + 1)
                .add(host())
                .addAll(participants())
                .build();
    }

    public PlayerRole getPlayerRole(@NotNull UUID playerId) {
        if (Objects.equals(playerId, host()))
            return PlayerRole.HOST;
        else if (participants().contains(playerId))
            return PlayerRole.PARTICIPANT;
        else
            return PlayerRole.NONE;
    }
}
