package xland.mcmod.neospeedzero.difficulty;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

import java.util.Optional;

public interface SpeedrunDifficulty {
    Codec<SpeedrunDifficulty> CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.comapFlatMap(
            id -> Optional.ofNullable(SpeedrunDifficulties.get(id))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "SpeedrunDifficulty not found: " + id)),
            SpeedrunDifficulty::id
    ));

    @NotNull Identifier id();

    default @NotNull Component displayedName() {
        return Component.literal(id().toString());
    }

    /**
     * The display name that can be hovered in the goal-starting dialog.
     * @since 6.0.1
     */
    @ApiStatus.Experimental
    default @NotNull Component displayedNameHoverable() {
        return this.displayedName();
    }

    void onStart(ServerPlayer player, SpeedrunRecord record);

    static SpeedrunDifficulty getDefault() {
        // Classic
        return BuiltinDifficulty.UU;
    }
}
