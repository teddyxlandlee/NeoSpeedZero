package xland.mcmod.neospeedzero.resource.loader.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;

public final class SpeedrunGoalManagerImpl extends SpeedrunGoalManager implements IdentifiableResourceReloadListener {
    private SpeedrunGoalManagerImpl(HolderLookup.Provider provider) {
        super(provider);
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(GOAL_KEY_ID, SpeedrunGoalManagerImpl::new);
    }

    @Override
    public ResourceLocation getFabricId() {
        return GOAL_KEY_ID;
    }
}
