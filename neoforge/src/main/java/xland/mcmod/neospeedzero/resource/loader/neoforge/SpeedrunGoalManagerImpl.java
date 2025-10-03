package xland.mcmod.neospeedzero.resource.loader.neoforge;

import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;

import java.util.function.Function;

@SuppressWarnings("unused")
public final class SpeedrunGoalManagerImpl {
    public static void register(Function<HolderLookup.Provider, SpeedrunGoalManager> factory) {
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            // Register to NeoForge bus
            event.addListener(SpeedrunGoalManager.GOAL_KEY_ID, factory.apply(event.getServerResources().getRegistryLookup()));
        });
    }
}
