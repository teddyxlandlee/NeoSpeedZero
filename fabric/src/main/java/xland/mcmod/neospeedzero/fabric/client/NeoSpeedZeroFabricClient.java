package xland.mcmod.neospeedzero.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import xland.mcmod.neospeedzero.NeoSpeedZeroClient;

@Environment(EnvType.CLIENT)
public final class NeoSpeedZeroFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        NeoSpeedZeroClient.initClient();
    }
}
