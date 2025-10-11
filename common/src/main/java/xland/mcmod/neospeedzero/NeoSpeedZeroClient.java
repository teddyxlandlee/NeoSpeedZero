package xland.mcmod.neospeedzero;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import xland.mcmod.neospeedzero.util.DurationLocalizer;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;
import xland.mcmod.neospeedzero.view.ViewPackets;

@Environment(EnvType.CLIENT)
public final class NeoSpeedZeroClient {
    public static final KeyMapping KEY_VIEW = new KeyMapping(
            "key.neospeedzero.view",
            GLFW.GLFW_KEY_B,
            KeyMapping.Category.MISC
    );

    private NeoSpeedZeroClient() {}

    public static void initClient() {
        // Key
        PlatformEvents.registerKeyMapping(KEY_VIEW);
        PlatformEvents.postClientTick(() -> {
            if (KEY_VIEW.consumeClick()) {
                PlatformNetwork.sendToServer(ViewPackets.Request.INSTANCE);
            }
        });
        // Time format
        DurationLocalizer.bootstrap();
    }
}
