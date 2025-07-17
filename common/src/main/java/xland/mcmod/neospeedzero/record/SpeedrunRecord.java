package xland.mcmod.neospeedzero.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import xland.mcmod.neospeedzero.NeoSpeedMessages;
import xland.mcmod.neospeedzero.difficulty.SpeedrunDifficulty;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;
import xland.mcmod.neospeedzero.util.TimeUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.stream.LongStream;

public record SpeedrunRecord(
        SpeedrunGoal.Holder goal,
        UUID recordId,
        @Unmodifiable List<SpeedrunChallenge> challenges,
        long[] collectedTimes,
        long startTime,
        MutableLong finishTime,
        SpeedrunDifficulty difficulty
) {
    public void markComplete(final int index, final long currentTime) {
        collectedTimes()[index] = currentTime;
    }

    public void markComplete(final int index, LongSupplier timeFactory) {
        markComplete(index, timeFactory.getAsLong());
    }

    public boolean shallComplete() {
        return Arrays.stream(collectedTimes()).allMatch(l -> l >= 0);
    }

    public boolean hasCompleted() {
        return finishTime().longValue() >= 0;
    }

    public int completedCount() {
        return ((int) Arrays.stream(collectedTimes()).filter(l -> l >= 0).count());
    }

    public int totalCount() {
        return challenges().size();
    }

    public Duration duration(long currentTime) {
        long ticks = currentTime - startTime();
        return Duration.ofSeconds(ticks / 20, (ticks % 20) * TimeUtil.TICK_TO_NANOS);
    }

    @Contract(value = "->new", pure = true)
    public Component snapshot() {
        return NeoSpeedMessages.snapshotFor(this);
    }

    @ApiStatus.Internal
    public SpeedrunRecord {
    }

    public SpeedrunRecord(
            SpeedrunGoal.Holder goal,
            UUID recordId,
            List<SpeedrunChallenge> challenges,
            long startTime,
            SpeedrunDifficulty difficulty
    ) {
        this(
                goal, recordId, challenges,
                initLA(challenges.size()),  // collectedTimes
                startTime,
                new MutableLong(-1),    // finishTime
                difficulty
        );
    }

    private static long[] initLA(int size) {
        long[] l = new long[size];
        Arrays.fill(l, -1);
        return l;
    }

    public static final MapCodec<SpeedrunRecord> MAP_CODEC = createMapCodec().validate(SpeedrunRecord::validate);
    public static final Codec<SpeedrunRecord> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);

    private static MapCodec<SpeedrunRecord> createMapCodec() {
        final Codec<MutableLong> mutableLongCodec = Codec.LONG.xmap(MutableLong::new, MutableLong::toLong);

        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                SpeedrunGoal.HOLDER_CODEC.fieldOf("goal").forGetter(SpeedrunRecord::goal),
                UUIDUtil.STRING_CODEC.fieldOf("recordId").forGetter(SpeedrunRecord::recordId),
                SpeedrunChallenge.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("challenges").forGetter(SpeedrunRecord::challenges),
                Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of).fieldOf("collectedTimes").forGetter(SpeedrunRecord::collectedTimes),
                Codec.LONG.fieldOf("startTime").forGetter(SpeedrunRecord::startTime),
                mutableLongCodec.fieldOf("finishTime").forGetter(SpeedrunRecord::finishTime),
                SpeedrunDifficulty.CODEC.fieldOf("difficulty").forGetter(SpeedrunRecord::difficulty)
        ).apply(instance, SpeedrunRecord::new));
    }

    public DataResult<SpeedrunRecord> validate() {
        if (challenges().size() != collectedTimes().length) {
            return DataResult.error(() -> "Different sizes between challenges (" + challenges().size() + ") and collectedTimes (" + collectedTimes().length + ')');
        }
        return DataResult.success(this);
    }
}
