package xland.mcmod.neospeedzero.record;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.api.SpeedrunStartupConfig;
import xland.mcmod.neospeedzero.difficulty.BuiltinDifficulty;
import xland.mcmod.neospeedzero.difficulty.SpeedrunDifficulty;
import xland.mcmod.neospeedzero.resource.GoalPredicate;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record SpeedrunStartupConfigImpl(
        @Override SpeedrunGoal.Holder goal,
        @Override SpeedrunDifficulty difficulty,
        UUID recordId
) implements SpeedrunStartupConfig {
    @Override
    public SpeedrunRecord createRecord(long currentTime) {
        SpeedrunGoal.Holder goal = goal();
        List<SpeedrunChallenge> challenges = goal.goal().predicates().stream().flatMap(GoalPredicate::stream).toList();
        return new SpeedrunRecord(
                goal, recordId(), challenges, currentTime, difficulty()
        );
    }

    public static final class BuilderImpl implements Builder {
        private SpeedrunGoal.Holder goal;
        // Here we have default difficulty
        private @NotNull SpeedrunDifficulty difficulty = BuiltinDifficulty.UU;

        @Override
        public Builder goal(SpeedrunGoal.@NotNull Holder goal) {
            Objects.requireNonNull(goal, "goal");
            this.goal = goal;
            return this;
        }

        @Override
        public Builder difficulty(@NotNull SpeedrunDifficulty difficulty) {
            Objects.requireNonNull(difficulty, "difficulty");
            this.difficulty = difficulty;
            return this;
        }

        @Override
        public SpeedrunStartupConfig build() {
            Objects.requireNonNull(goal, "Missing goal");
            final UUID recordId = UUID.randomUUID();
            return new SpeedrunStartupConfigImpl(goal, difficulty, recordId);
        }

        private static final DynamicCommandExceptionType EX_INVALID_GOAL = new DynamicCommandExceptionType(
                obj -> Component.translatable("command.neospeedzero.not_found.goal", String.valueOf(obj))
        );
        private static final DynamicCommandExceptionType EX_INVALID_DIFFICULTY = new DynamicCommandExceptionType(
                obj -> Component.translatable("command.neospeedzero.not_found.difficulty", String.valueOf(obj))
        );

        @Override
        public Builder goal(ResourceLocation id) throws CommandSyntaxException {
            SpeedrunGoal.Holder holder = SpeedrunGoal.Holder.holders().get(id);
            if (holder == null) throw EX_INVALID_GOAL.create(id);
            return this.goal(holder);
        }

        @Override
        public Builder difficulty(ResourceLocation id) throws CommandSyntaxException {
            SpeedrunDifficulty speedrunDifficulty = SpeedrunDifficulties.get(id);
            if (speedrunDifficulty == null) throw EX_INVALID_DIFFICULTY.create(id);
            return this.difficulty(speedrunDifficulty);
        }
    }
}
