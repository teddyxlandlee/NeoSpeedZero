package xland.mcmod.neospeedzero.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SpeedrunGoal(ItemStack icon, Component display, List<GoalPredicate> predicates) {
    public static final Codec<SpeedrunGoal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("icon").forGetter(SpeedrunGoal::icon),
            ComponentSerialization.CODEC.fieldOf("display").forGetter(SpeedrunGoal::display),
            GoalPredicate.CODEC.listOf().fieldOf("predicates").forGetter(SpeedrunGoal::predicates)
    ).apply(instance, SpeedrunGoal::new));

    public static final Codec<Holder> HOLDER_CODEC = Codec.lazyInitialized(() -> {
        // Stored as a resource location
        return ResourceLocation.CODEC.comapFlatMap(
                id -> Optional.ofNullable(Holder.holders().get(id))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Can't find SpeedrunGoal.Holder " + id)),
                Holder::id
        );
    });

    public record Holder(ResourceLocation id, SpeedrunGoal goal) {
        private static Map<ResourceLocation, Holder> wrappedHolders = Collections.emptyMap();

        public static @Unmodifiable Map<ResourceLocation, Holder> holders() {
            return Collections.unmodifiableMap(wrappedHolders);
        }

        public static void clearHolders() {
            wrappedHolders = Collections.emptyMap();
        }

        public static void setHolders(Map<ResourceLocation, Holder> holders) {
            wrappedHolders = holders;
        }
    }
}
