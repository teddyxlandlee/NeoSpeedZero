package xland.mcmod.neospeedzero.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unused")
public class DurationLocalizerImpl {
    private DurationLocalizerImpl() {}

    public static boolean isLangPatchAvailable() {
        return FabricLoader.getInstance().isModLoaded("enchlevel-langpatch");
    }
}
