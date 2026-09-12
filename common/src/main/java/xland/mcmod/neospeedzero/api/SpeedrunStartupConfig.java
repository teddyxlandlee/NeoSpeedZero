package xland.mcmod.neospeedzero.api;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.Identifier;
import xland.mcmod.neospeedzero.difficulty.SpeedrunDifficulty;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.record.SpeedrunStartupConfigImpl;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

public interface SpeedrunStartupConfig {
    SpeedrunGoal.Holder goal();

    SpeedrunDifficulty difficulty();

    interface Builder {
        Builder goal(SpeedrunGoal.Holder goal);

        Builder difficulty(SpeedrunDifficulty difficulty);

        SpeedrunStartupConfig build();

        Builder goal(Identifier id) throws CommandSyntaxException;

        Builder difficulty(Identifier id) throws CommandSyntaxException;
    }

    static Builder builder() {
        return new SpeedrunStartupConfigImpl.BuilderImpl();
    }

    SpeedrunRecord createRecord(long currentTime);
}
