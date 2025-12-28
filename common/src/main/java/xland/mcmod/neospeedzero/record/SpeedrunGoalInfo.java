package xland.mcmod.neospeedzero.record;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

import java.util.function.Function;
import java.util.stream.Stream;

public interface SpeedrunGoalInfo {
    Identifier id();

    ItemStack icon();

    Component display();

    @ApiStatus.Internal
    record Impl(Identifier id, ItemStack icon, Component display) implements SpeedrunGoalInfo {
        public static Impl of(SpeedrunGoalInfo info) {
            if (info instanceof Impl) return (Impl) info;
            return new Impl(info.id(), info.icon(), info.display());
        }

        private static final MapCodec<SpeedrunGoalInfo> MAP_CODEC_IMPL = RecordCodecBuilder.<Impl>mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("goal").forGetter(SpeedrunGoalInfo.Impl::id),
                ItemStack.CODEC.fieldOf("goal_icon").forGetter(SpeedrunGoalInfo.Impl::icon),
                ComponentSerialization.CODEC.fieldOf("goal_display").forGetter(SpeedrunGoalInfo.Impl::display)
        ).apply(instance, Impl::new)).xmap(Function.identity(), Impl::of);

        public static final MapCodec<SpeedrunGoalInfo> MAP_CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return MAP_CODEC_IMPL.keys(ops);
            }

            private static final MapCodec<Identifier> GOAL_ONLY = Identifier.CODEC.fieldOf("goal");

            @Override
            public <T> DataResult<SpeedrunGoalInfo> decode(DynamicOps<T> ops, MapLike<T> input) {
                var fullResult = MAP_CODEC_IMPL.decode(ops, input);
                if (fullResult.isSuccess()) return fullResult;

                var goalOnlyResult = GOAL_ONLY.decode(ops, input);
                if (goalOnlyResult.isError()) return DataResult.error(goalOnlyResult.error().orElseThrow().messageSupplier());

                Identifier goalId = goalOnlyResult.getOrThrow();

                try {
                    @Nullable SpeedrunGoal.Holder holder = SpeedrunGoal.Holder.holders().get(goalId);
                    if (holder != null) return DataResult.success(holder);
                    return DataResult.error(() -> "No speedrun goal found with id " + goalId, partialResultOf(goalId));
                } catch (Exception e) {
                    return DataResult.error(() -> "Error while getting speedrun goal with id " + goalId + ": " + e, partialResultOf(goalId));
                }
            }

            private static SpeedrunGoalInfo partialResultOf(Identifier id) {
                return new Impl(id, Items.BARRIER.getDefaultInstance(), Component.literal(id.toString()));
            }

            @Override
            public <T> RecordBuilder<T> encode(SpeedrunGoalInfo input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return MAP_CODEC_IMPL.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "SpeedrunGoalInfo.Impl.MAP_CODEC";
            }
        };
    }
}
