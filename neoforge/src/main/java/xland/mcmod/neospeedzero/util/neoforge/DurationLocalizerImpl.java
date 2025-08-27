package xland.mcmod.neospeedzero.util.neoforge;

import net.neoforged.fml.ModList;

@SuppressWarnings("unused")
public final class DurationLocalizerImpl {
    private DurationLocalizerImpl() {}

    public static boolean isLangPatchAvailable() {
        return ModList.get().isLoaded("enchlevellangpatch");
    }
}
