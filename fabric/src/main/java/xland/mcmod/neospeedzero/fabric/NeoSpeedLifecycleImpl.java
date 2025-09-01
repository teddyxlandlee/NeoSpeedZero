package xland.mcmod.neospeedzero.fabric;

import dev.architectury.event.events.common.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public final class NeoSpeedLifecycleImpl {
    public static void registerAdvancementEvent(@NotNull PlayerEvent.PlayerAdvancement callback) {
        Objects.requireNonNull(callback, "Callback must not be null");
        // Fabric implementation of Architectury is okay
        PlayerEvent.PLAYER_ADVANCEMENT.register(callback);
    }

    private NeoSpeedLifecycleImpl() {}
}
