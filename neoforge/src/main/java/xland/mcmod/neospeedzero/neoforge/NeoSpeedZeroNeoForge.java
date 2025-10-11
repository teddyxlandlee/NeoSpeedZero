package xland.mcmod.neospeedzero.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import net.neoforged.fml.common.Mod;
import xland.mcmod.neospeedzero.NeoSpeedZeroClient;

@Mod(NeoSpeedZero.MOD_ID)
public final class NeoSpeedZeroNeoForge {
    public NeoSpeedZeroNeoForge(IEventBus modBus) {
        // Run our common setup.
        NeoSpeedZero.init();
        modBus.addListener(FMLClientSetupEvent.class, NeoSpeedZeroNeoForge::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NeoSpeedZeroClient::initClient);
    }
}
