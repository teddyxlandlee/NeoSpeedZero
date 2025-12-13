package xland.mcmod.neospeedzero.resource.loader;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.Map;

@org.jspecify.annotations.NullMarked
public class SpeedrunGoalManager extends SimpleJsonResourceReloadListener<SpeedrunGoal> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier GOAL_KEY_ID = Identifier.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, "goals");
    public static final ResourceKey<Registry<SpeedrunGoal>> GOAL_KEY = ResourceKey.createRegistryKey(GOAL_KEY_ID);

    private SpeedrunGoalManager(HolderLookup.Provider provider) {
        super(provider, SpeedrunGoal.CODEC, GOAL_KEY);
    }

    public static void registerEvents() {
        PlatformEvents.getInstance().registerResourceReloadListener(GOAL_KEY_ID, SpeedrunGoalManager::new);
    }

    @Override
    protected void apply(Map<Identifier, SpeedrunGoal> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        final ImmutableMap.Builder<Identifier, SpeedrunGoal.Holder> builder = ImmutableMap.builder();
        map.forEach((id, goal) -> builder.put(id, new SpeedrunGoal.Holder(id, goal)));
        SpeedrunGoal.Holder.setHolders(builder.buildOrThrow());
        LOGGER.info("Updated SpeedrunGoal.Holder");
    }
}
