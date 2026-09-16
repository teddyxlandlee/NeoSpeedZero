package xland.mcmod.neospeedzero;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import xland.mcmod.neospeedzero.util.DurationLocalizer;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;
import xland.mcmod.neospeedzero.view.ViewPackets;

@Environment(EnvType.CLIENT)
public final class NeoSpeedZeroClient {
    public static final KeyMapping KEY_VIEW = new KeyMapping(
            "key.neospeedzero.view",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_B,
            KeyMapping.Category.MISC
    );

    private NeoSpeedZeroClient() {}

    public static void initClient() {
        // Key
        PlatformEvents.getInstance().registerKeyMapping(KEY_VIEW);
        PlatformEvents.getInstance().postClientTick(() -> {
            if (KEY_VIEW.consumeClick()) {
                PlatformNetwork.getInstance().sendToServer(ViewPackets.Request.INSTANCE);
            }
        });
    }

    public static void initLangPatch() {
        // Time format
        DurationLocalizer.bootstrap();
    }
}
