package xland.mcmod.neospeedzero.resource;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface GoalPredicate permits GoalPredicate.OfItemPredicate, GoalPredicate.OfAdvancement {
    Optional<StatedIcon> icon();

    Stream<SpeedrunChallenge> stream();

    abstract sealed class OfItemPredicate implements GoalPredicate permits ItemGoalPredicate, OfItemPredicate.Basic {
        private final @Nullable StatedIcon statedIcon;
        protected final @Nullable ItemPredicate subPredicate;

        protected OfItemPredicate(Optional<StatedIcon> statedIcon, Optional<ItemPredicate> subPredicate) {
            this.statedIcon = statedIcon.orElse(null);
            this.subPredicate = subPredicate.orElse(null);
        }

        @Override
        public Optional<StatedIcon> icon() {
            return Optional.ofNullable(statedIcon);
        }

        public static ItemStack theAnyApple() {
            ItemStack stack = new ItemStack(Items.APPLE);
            stack.set(DataComponents.ITEM_NAME, Component.translatable("item_predicate.neospeedzero.extra_req.items.any"));
            return stack;
        }

        @Override
        public Stream<SpeedrunChallenge> stream() {
            ItemPredicate itemPredicate = subPredicate;
            if (itemPredicate == null) {
                itemPredicate = ItemPredicate.Builder.item().build();   // Any
            }

            return Stream.of(SpeedrunChallenge.of(Either.left(itemPredicate), theAnyApple(), icon()));
        }

        static final class Basic extends OfItemPredicate {
            Basic(Optional<StatedIcon> statedIcon, Optional<ItemPredicate> subPredicate) {
                super(statedIcon, subPredicate);
            }

            public static final Codec<Basic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StatedIcon.CODEC.optionalFieldOf("icon").forGetter(OfItemPredicate::icon),
                    ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(p -> Optional.ofNullable(p.subPredicate))
            ).apply(instance, Basic::new));
        }

        // Avoid classloading deadlock
        public static final Codec<OfItemPredicate> CODEC = Codec.lazyInitialized(() -> Codec.either(/*primary=*/ItemGoalPredicate.CODEC, /*secondary=*/Basic.CODEC).xmap(
                either -> either.map(Function.identity(), Function.identity()),
                p -> switch (p) {
                    case ItemGoalPredicate itemGoalPredicate -> Either.left(itemGoalPredicate);
                    case Basic basic -> Either.right(basic);
                }
        ));
    }

    record OfAdvancement(ResourceKey<Advancement> advancementKey, @Override Optional<StatedIcon> icon) implements GoalPredicate {
        public static final Codec<OfAdvancement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.ADVANCEMENT).fieldOf("advancement").forGetter(OfAdvancement::advancementKey),
                StatedIcon.CODEC.optionalFieldOf("icon").forGetter(OfAdvancement::icon)
        ).apply(instance, OfAdvancement::new));

        public static ItemStack generatedIcon() {
            return Items.GRASS_BLOCK.getDefaultInstance();
        }

        @Override
        public Stream<SpeedrunChallenge> stream() {
            return Stream.of(SpeedrunChallenge.of(Either.right(advancementKey), generatedIcon(), icon));
        }
    }

    Codec<GoalPredicate> CODEC = Codec.lazyInitialized(() -> Codec.either(OfAdvancement.CODEC, OfItemPredicate.CODEC).xmap(
            either -> either.map(Function.identity(), Function.identity()),
            p -> switch (p) {   // sealed
                case OfAdvancement ofAdvancement -> Either.left(ofAdvancement);
                case OfItemPredicate ofItemPredicate -> Either.right(ofItemPredicate);
            }
    ));
}
