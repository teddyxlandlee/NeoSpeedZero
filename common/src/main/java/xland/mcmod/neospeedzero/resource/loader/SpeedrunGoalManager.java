package xland.mcmod.neospeedzero.resource.loader;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

import java.util.Map;

public class SpeedrunGoalManager extends SimpleJsonResourceReloadListener<SpeedrunGoal> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation GOAL_KEY_ID = ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "goals");
    public static final ResourceKey<Registry<SpeedrunGoal>> GOAL_KEY = ResourceKey.createRegistryKey(GOAL_KEY_ID);

    protected SpeedrunGoalManager(HolderLookup.Provider provider) {
        super(provider, SpeedrunGoal.CODEC, GOAL_KEY);
    }

    public static void registerEvents() {
        register();
        // Remove cached holders
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            LOGGER.info("Clearing SpeedRunGoal.Holder");
            SpeedrunGoal.Holder.clearHolders();
        });
    }

    @ExpectPlatform
    private static void register() { throw new AssertionError("ExpectPlatform"); }

    @Override
    protected void apply(Map<ResourceLocation, SpeedrunGoal> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        final ImmutableMap.Builder<ResourceLocation, SpeedrunGoal.Holder> builder = ImmutableMap.builder();
        map.forEach((id, goal) -> builder.put(id, new SpeedrunGoal.Holder(id, goal)));
        SpeedrunGoal.Holder.setHolders(builder.buildOrThrow());
        LOGGER.info("Updated SpeedrunGoal.Holder");
        xland.mcmod.neospeedzero.util.ABSDebug.debug(1, l -> {
            l.info("Known keys: {}", map.keySet());
            var loaded = net.minecraft.resources.FileToIdConverter.registry(GOAL_KEY).listMatchingResources(resourceManager);
            l.info("Load again ({}): {}", loaded.size(), loaded);
            var path = net.minecraft.core.registries.Registries.elementsDirPath(GOAL_KEY);
            var path2 = GOAL_KEY_ID.getPath();
            l.info("Path#1: {}, Path#2: {}, Equal: {}", path, path2, java.util.Objects.equals(path, path2));
            loaded = net.minecraft.resources.FileToIdConverter.json(path).listMatchingResources(resourceManager);
            l.info("Load another time ({}): {}", loaded.size(), loaded);
        });
    }
}
