package xland.mcmod.neospeedzero.resource.loader.neoforge;

import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;

public final class SpeedrunGoalManagerImpl extends SpeedrunGoalManager {
    private SpeedrunGoalManagerImpl(HolderLookup.Provider provider) {
        super(provider);
    }

    @SuppressWarnings("unused")
    public static void register() {
        NeoForge.EVENT_BUS.addListener(SpeedrunGoalManagerImpl::onListenerAdd);
    }

    private static void onListenerAdd(AddServerReloadListenersEvent event) {
        event.addListener(GOAL_KEY_ID, new SpeedrunGoalManagerImpl(event.getServerResources().getRegistryLookup()));
    }
}
