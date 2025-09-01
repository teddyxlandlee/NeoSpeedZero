package xland.mcmod.neospeedzero.neoforge;

import com.google.common.base.Preconditions;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class NeoSpeedLifecycleImpl {
    private static final AtomicReference<PlayerEvent.PlayerAdvancement> theCallbackRef = new AtomicReference<>();

    public static void registerAdvancementEvent(@NotNull PlayerEvent.PlayerAdvancement callback) {
        Preconditions.checkNotNull(callback, "Callback must not be null");

        if (!theCallbackRef.compareAndSet(null, callback)) {
            throw new IllegalStateException("theCallback is initialized twice");
        }
    }

    // Will be invoked via coremod
    public static void onAdvancementMade(ServerPlayer player, AdvancementHolder holder) {
        PlayerEvent.PlayerAdvancement callback = theCallbackRef.get();
        Preconditions.checkState(callback != null, "theCallback is not initialized yet");
        callback.award(player, holder);
    }

    private NeoSpeedLifecycleImpl() {}
}