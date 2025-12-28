package xland.mcmod.neospeedzero.neoforge;

import net.neoforged.fml.ModList;
import xland.mcmod.neospeedzero.neoforge.event.PlatformEventsNeoForge;
import xland.mcmod.neospeedzero.neoforge.network.PlatformNetworkNeoForge;
import xland.mcmod.neospeedzero.util.PlatformAPI;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

@PlatformAPI.Implementation(PlatformAPI.Platform.NEO)
public final class NeoPlatform extends PlatformAPI {
    @Override
    public boolean isLangPatchAvailable() {
        return ModList.get().isLoaded("enchlevellangpatch");
    }

    @Override
    public PlatformEvents events() {
        return PlatformEventsNeoForge.getInstance();
    }

    @Override
    public PlatformNetwork network() {
        return PlatformNetworkNeoForge.getInstance();
    }
}
