package xland.mcmod.neospeedzero.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import net.fabricmc.api.ModInitializer;
import xland.mcmod.neospeedzero.util.DurationLocalizer;
import xland.mcmod.neospeedzero.util.PlatformDependent;

public final class NeoSpeedZeroFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        NeoSpeedZero.init();
    }

    @ApiStatus.Internal
    @PlatformDependent(PlatformDependent.Platform.FABRIC)
    public static final class LangPatchProberImpl implements DurationLocalizer.LangPatchProber {
        @Override
        public boolean isLangPatchAvailable() {
            return FabricLoader.getInstance().isModLoaded("enchlevel-langpatch");
        }
    }
}
