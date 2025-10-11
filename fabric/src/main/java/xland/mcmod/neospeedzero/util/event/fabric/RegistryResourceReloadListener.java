package xland.mcmod.neospeedzero.util.event.fabric;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
final class RegistryResourceReloadListener implements PreparableReloadListener {
    private final Function<HolderLookup.Provider, PreparableReloadListener> factory;

    RegistryResourceReloadListener(Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        Objects.requireNonNull(factory, "factory cannot be null.");
        this.factory = factory;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
        HolderLookup.Provider provider = sharedState.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
        return factory.apply(provider).reload(sharedState, executor, preparationBarrier, executor2);
    }
}
