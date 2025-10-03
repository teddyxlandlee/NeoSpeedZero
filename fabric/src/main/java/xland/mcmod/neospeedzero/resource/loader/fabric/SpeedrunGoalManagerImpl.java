package xland.mcmod.neospeedzero.resource.loader.fabric;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.resource.loader.SpeedrunGoalManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
public final class SpeedrunGoalManagerImpl implements PreparableReloadListener {
    private final Function<HolderLookup.Provider, SpeedrunGoalManager> factory;

    private SpeedrunGoalManagerImpl(Function<HolderLookup.Provider, SpeedrunGoalManager> factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unused")
    public static void register(Function<HolderLookup.Provider, SpeedrunGoalManager> factory) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(SpeedrunGoalManager.GOAL_KEY_ID, new SpeedrunGoalManagerImpl(factory));
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        HolderLookup.Provider provider = sharedState.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
        return factory.apply(provider).reload(sharedState, executor, preparationBarrier, executor2);
    }
}
