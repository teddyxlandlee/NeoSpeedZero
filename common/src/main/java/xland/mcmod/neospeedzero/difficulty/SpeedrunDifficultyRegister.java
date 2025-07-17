package xland.mcmod.neospeedzero.difficulty;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xland.mcmod.neospeedzero.NeoSpeedZero;

@Deprecated
public final class SpeedrunDifficultyRegister {
    private SpeedrunDifficultyRegister() {}

    public static final ResourceKey<Registry<SpeedrunDifficulty>> RESOURCE_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "difficulty"));
    public static final DeferredRegister<SpeedrunDifficulty> REGISTER = DeferredRegister.create(NeoSpeedZero.MOD_ID, RESOURCE_KEY);

    static {
        REGISTER.getRegistrarManager().<SpeedrunDifficulty>builder(RESOURCE_KEY.location()).build();
    }

    public static void register() {

    }
}
