package xland.mcmod.neospeedzero.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import net.neoforged.fml.common.Mod;
import xland.mcmod.neospeedzero.NeoSpeedZeroClient;
import xland.mcmod.neospeedzero.neoforge.event.PlatformEventsNeoForge;
import xland.mcmod.neospeedzero.util.DurationLocalizer;
import xland.mcmod.neospeedzero.util.PlatformDependent;

@Mod(NeoSpeedZero.MOD_ID)
public final class NeoSpeedZeroNeoForge {
    public NeoSpeedZeroNeoForge(IEventBus modBus, Dist dist) {
        // Run our common setup.
        NeoSpeedZero.init();
        modBus.addListener(FMLClientSetupEvent.class, NeoSpeedZeroNeoForge::onClientSetup);
        PlatformEventsNeoForge.GAME_RULE_REG.register(modBus);
        if (dist.isClient()) {
            NeoSpeedZeroClient.initLangPatch();
        }
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NeoSpeedZeroClient::initClient);
    }

    @PlatformDependent(PlatformDependent.Platform.NEO)
    public static final class LangPatchProberImpl implements DurationLocalizer.LangPatchProber {
        @Override
        public boolean isLangPatchAvailable() {
            return ModList.get().isLoaded("enchlevellangpatch");
        }
    }
}
