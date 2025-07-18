package xland.mcmod.neospeedzero;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import xland.mcmod.neospeedzero.view.ViewPackets;

@Environment(EnvType.CLIENT)
public final class NeoSpeedZeroClient {
    public static final KeyMapping KEY_VIEW = new KeyMapping(
            "key.neospeedzero.view",
            GLFW.GLFW_KEY_B,
            KeyMapping.CATEGORY_MISC
    );

    private NeoSpeedZeroClient() {}

    public static void initClient() {
        // Key
        KeyMappingRegistry.register(KEY_VIEW);
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (KEY_VIEW.consumeClick()) {
                NetworkManager.sendToServer(ViewPackets.Request.INSTANCE);
            }
        });
    }
}
