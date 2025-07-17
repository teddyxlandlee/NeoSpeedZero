package xland.mcmod.neospeedzero.resource;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.record.SpeedrunChallenge;

import java.util.Optional;
import java.util.stream.Stream;

final class ItemGoalPredicate extends GoalPredicate.OfItemPredicate {
    private final @NotNull HolderSet<Item> items;
    private final @NotNull Select select;

    ItemGoalPredicate(@NotNull HolderSet<Item> items, @NotNull Select select, Optional<StatedIcon> statedIcon, Optional<ItemPredicate> subPredicate) {
        super(statedIcon, subPredicate);
        this.items = items;
        this.select = select;
    }

    static ItemGoalPredicate of(@NotNull HolderSet<Item> items) {
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

    private @NotNull HolderSet<Item> items() {
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
                ItemStack generatedIcon = theAnyApple();
                ExtraRequirements.fillExtraRequirements(generatedIcon, items().unwrap().mapRight(HolderSet::direct), subPredicate);
                yield Stream.of(SpeedrunChallenge.of(Either.left(newPredicate), generatedIcon, this.icon()));
            }
            case ALL -> this.items().stream().map(itemHolder -> {
                ItemStack generatedIcon = new ItemStack(itemHolder);
                HolderSet.Direct<Item> holderSet = HolderSet.direct(itemHolder);
                ItemPredicate newPredicate = new ItemPredicate(
                        Optional.of(holderSet),
                        subPredicate != null ? subPredicate.count() : MinMaxBounds.Ints.ANY,
                        subPredicate != null ? subPredicate.components() : DataComponentMatchers.ANY
                );
                ExtraRequirements.fillExtraRequirements(generatedIcon, null, subPredicate);
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
        public @NotNull String getSerializedName() {
            return id;
        }

        static final EnumCodec<Select> CODEC = StringRepresentable.fromEnum(Select::values);
    }
}
