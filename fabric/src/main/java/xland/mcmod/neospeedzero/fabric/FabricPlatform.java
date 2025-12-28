package xland.mcmod.neospeedzero.fabric;

import net.fabricmc.loader.api.FabricLoader;
import xland.mcmod.neospeedzero.fabric.event.PlatformEventsFabric;
import xland.mcmod.neospeedzero.fabric.network.PlatformNetworkFabric;
import xland.mcmod.neospeedzero.util.PlatformAPI;

import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

@PlatformAPI.Implementation(PlatformAPI.Platform.FABRIC)
public final class FabricPlatform extends PlatformAPI {
    @Override
    public boolean isLangPatchAvailable() {
        return FabricLoader.getInstance().isModLoaded("enchlevel-langpatch");
    }

    @Override
    public PlatformEvents events() {
        return PlatformEventsFabric.getInstance();
    }

    @Override
    public PlatformNetwork network() {
        return PlatformNetworkFabric.getInstance();
    }
}
