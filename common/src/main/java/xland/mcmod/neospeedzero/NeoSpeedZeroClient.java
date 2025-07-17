package xland.mcmod.neospeedzero;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xland.mcmod.neospeedzero.view.ChallengeSnapshot;
import xland.mcmod.neospeedzero.view.ViewChallengeScreen;
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
        // Network
        NetworkManager.registerReceiver(
                NetworkManager.s2c(), ViewPackets.TYPE_SNAPSHOT, ChallengeSnapshot.STREAM_CODEC,
                (snapshot, context) -> context.queue(() -> {
                    // Received snapshot -> open the screen
                    Minecraft.getInstance().setScreen(new ViewChallengeScreen(snapshot));
                })
        );
        NetworkManager.registerReceiver(
                NetworkManager.s2c(), ViewPackets.TYPE_CHANGE, ChallengeSnapshot.Change.STREAM_CODEC,
                (change, context) -> context.queue(() -> {
                    if (Minecraft.getInstance().screen instanceof ViewChallengeScreen viewChallengeScreen) {
                        viewChallengeScreen.onDataUpdate(change);
                    }
                })
        );
        // Key
        KeyMappingRegistry.register(KEY_VIEW);
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (KEY_VIEW.consumeClick()) {
                NetworkManager.sendToServer(ViewPackets.Request.INSTANCE);
            }
        });
    }
}
