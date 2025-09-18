package xland.mcmod.neospeedzero.record.manager;

import com.google.common.collect.ImmutableSet;
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
    public static final StreamCodec<ByteBuf, SpeedrunPlayerInfo> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SpeedrunPlayerInfo::host,
            ByteBufCodecs.collection(LinkedHashSet::new, UUIDUtil.STREAM_CODEC), SpeedrunPlayerInfo::participants,
            SpeedrunPlayerInfo::new
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
