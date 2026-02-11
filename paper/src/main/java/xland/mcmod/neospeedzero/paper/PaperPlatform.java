package xland.mcmod.neospeedzero.paper;

import xland.mcmod.neospeedzero.util.PlatformAPI;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

@PlatformAPI.Implementation(PlatformAPI.Platform.PAPER)
public final class PaperPlatform extends PlatformAPI {
    @Override
    public boolean isLangPatchAvailable() {
        return false;   // since we are always on dedicated server
    }

    @Override
    public PlatformEvents events() {
        return PaperEvents.getInstance();
    }

    @Override
    public PlatformNetwork network() {
        return PaperNetwork.getInstance();
    }
}
