package xland.mcmod.neospeedzero.api;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.difficulty.BuiltinDifficulty;
import xland.mcmod.neospeedzero.difficulty.SpeedrunDifficulty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public final class SpeedrunDifficulties {
    private static final ConcurrentMap<Identifier, SpeedrunDifficulty> REGISTRY_MAP = Maps.newConcurrentMap();

    public static void register(SpeedrunDifficulty difficulty) {
        if (REGISTRY_MAP.put(difficulty.id(), difficulty) != null) {
            throw new IllegalStateException("Duplicate registry of " + difficulty.id());
        }
    }

    @Nullable
    public static SpeedrunDifficulty get(Identifier id) {
        return REGISTRY_MAP.get(id);
    }

    @ApiStatus.Internal
    public static void registerBuiltins() {
        for (BuiltinDifficulty difficulty : BuiltinDifficulty.values()) {
            register(difficulty);
        }
    }

    public static Set<Identifier> keys() {
        return Collections.unmodifiableSet(REGISTRY_MAP.keySet());
    }

    public static Set<Map.Entry<Identifier, SpeedrunDifficulty>> entries() {
        return Collections.unmodifiableSet(REGISTRY_MAP.entrySet());
    }
}
