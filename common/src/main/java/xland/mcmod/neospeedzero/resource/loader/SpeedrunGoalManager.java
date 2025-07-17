package xland.mcmod.neospeedzero.resource.loader;

import com.google.common.collect.ImmutableMap;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

import java.util.Map;

public class SpeedrunGoalManager extends SimpleJsonResourceReloadListener<SpeedrunGoal> {
    public static final ResourceLocation GOAL_KEY_ID = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "speedrun/goals");
    public static final ResourceKey<Registry<SpeedrunGoal>> GOAL_KEY = ResourceKey.createRegistryKey(GOAL_KEY_ID);

    protected SpeedrunGoalManager(HolderLookup.Provider provider) {
        super(provider, SpeedrunGoal.CODEC, GOAL_KEY);
    }

    public static void registerEvents() {
        register();
        // Remove cached holders
        LifecycleEvent.SERVER_STOPPED.register(server -> SpeedrunGoal.Holder.clearHolders());
    }

    @ExpectPlatform
    private static void register() { throw new AssertionError("ExpectPlatform"); }

    @Override
    protected void apply(Map<ResourceLocation, SpeedrunGoal> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        final ImmutableMap.Builder<ResourceLocation, SpeedrunGoal.Holder> builder = ImmutableMap.builder();
        map.forEach((id, goal) -> builder.put(id, new SpeedrunGoal.Holder(id, goal)));
        SpeedrunGoal.Holder.setHolders(builder.buildOrThrow());
    }
}
