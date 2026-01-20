package xland.mcmod.neospeedzero.resource;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;

import java.util.Optional;
import java.util.stream.Stream;

@org.jspecify.annotations.NullMarked
final class ItemGoalPredicate extends GoalPredicate.OfItemPredicate {
    private final HolderSet<Item> items;
    private final Select select;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ItemGoalPredicate(HolderSet<Item> items, Select select, Optional<StatedIcon> statedIcon, Optional<ItemPredicate> subPredicate) {
        super(statedIcon, subPredicate);
        this.items = items;
        this.select = select;
    }

    static ItemGoalPredicate of(HolderSet<Item> items) {
        return new ItemGoalPredicate(items, Select.ALL, Optional.empty(), Optional.empty());
    }

    public static final Codec<ItemGoalPredicate> CODEC = createCodec();

    private static Codec<ItemGoalPredicate> createCodec() {
        Codec<ItemGoalPredicate> baseCodec = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ItemGoalPredicate::items),
                Select.CODEC.optionalFieldOf("select", Select.ALL).forGetter(p -> p.select),
                StatedIcon.CODEC.optionalFieldOf("icon").forGetter(ItemGoalPredicate::icon),
                ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(p -> Optional.ofNullable(p.subPredicate))
        ).apply(instance, ItemGoalPredicate::new));
        // alternative: string/array
        return Codec.withAlternative(baseCodec, RegistryCodecs.homogeneousList(Registries.ITEM), ItemGoalPredicate::of);
    }

    private HolderSet<Item> items() {
        return items;
    }

    @Override
    public Stream<SpeedrunChallenge> stream() {
        return switch (this.select) {
            case ANY -> {
                ItemPredicate newPredicate = new ItemPredicate(
                        Optional.of(this.items()),
                        subPredicate != null ? subPredicate.count() : MinMaxBounds.Ints.ANY,
                        subPredicate != null ? subPredicate.components() : DataComponentMatchers.ANY
                );
                ItemStackTemplate generatedIcon = theAnyApple();
                generatedIcon = ExtraRequirements.fillExtraRequirements(generatedIcon, items().unwrap().mapRight(HolderSet::direct), subPredicate);
                yield Stream.of(SpeedrunChallenge.of(Either.left(newPredicate), generatedIcon, this.icon()));
            }
            case ALL -> this.items().stream().map(itemHolder -> {
                ItemStackTemplate generatedIcon = new ItemStackTemplate(itemHolder, 1, DataComponentPatch.EMPTY);
                HolderSet.Direct<Item> holderSet = HolderSet.direct(itemHolder);
                ItemPredicate newPredicate = new ItemPredicate(
                        Optional.of(holderSet),
                        subPredicate != null ? subPredicate.count() : MinMaxBounds.Ints.ANY,
                        subPredicate != null ? subPredicate.components() : DataComponentMatchers.ANY
                );
                generatedIcon = ExtraRequirements.fillExtraRequirements(generatedIcon, null, subPredicate);
                return SpeedrunChallenge.of(Either.left(newPredicate), generatedIcon, this.icon());
            });
        };
    }

    enum Select implements StringRepresentable {
        ANY("any"),
        ALL("all"),
        ;
        private final String id;

        Select(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        static final EnumCodec<Select> CODEC = StringRepresentable.fromEnum(Select::values);
    }
}
