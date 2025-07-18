package xland.mcmod.neospeedzero.resource;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.mixin.EnchantmentsPredicateAccessor;

import java.util.*;
import java.util.function.Consumer;

final class ExtraRequirements {
    static void fillExtraRequirements(ItemStack stack,
                                      @Nullable Either<TagKey<Item>, HolderSet<Item>> ofAny,
                                      @Nullable ItemPredicate predicate
    ) {
        List<Component> appendedLores = new ArrayList<>();

        if (predicate != null) {
            // Count
            MinMaxBounds.Ints count = predicate.count();
            if (!count.isAny())
                appendedLores.add(Component.translatable("item_predicate.neospeedzero.extra_req.count", formatIntRange(count)));

            DataComponentPatch componentChanges = predicate.components().exact().asPatch();
            Map<DataComponentPredicate.Type<?>, DataComponentPredicate> partial = predicate.components().partial();
            // Damage
            componentFromChanges(componentChanges, DataComponents.DAMAGE).ifPresentOrElse(damage -> {
                // Exact damage predicate
                appendedLores.add(Component.translatable("item_predicate.neospeedzero.extra_req.damage", formatNumber(damage, damage)));
            }, () -> {
                if (partial.get(DataComponentPredicates.DAMAGE) instanceof DamagePredicate(
                        MinMaxBounds.Ints durability, MinMaxBounds.Ints damage
                )) {
                    appendedLores.add(Component.translatable("item_predicate.neospeedzero.extra_req.durability", formatIntRange(durability)));
                    appendedLores.add(Component.translatable("item_predicate.neospeedzero.extra_req.damage", formatIntRange(damage)));
                }
            });
            // Enchantments
            readEnchantments(componentChanges, DataComponents.STORED_ENCHANTMENTS, partial.get(DataComponentPredicates.STORED_ENCHANTMENTS), appendedLores::add);
            readEnchantments(componentChanges, DataComponents.ENCHANTMENTS, partial.get(DataComponentPredicates.ENCHANTMENTS), appendedLores::add);

            // Custom Data

            // TODO: fill other extra requirements
        }

        if (!appendedLores.isEmpty()) {
            List<Component> lines = Optional.ofNullable(stack.get(DataComponents.LORE))
                    .map(ItemLore::lines)
                    .orElse(Collections.emptyList());
            lines = new ArrayList<>(lines);
            lines.addAll(appendedLores);
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
    }

    private static void readEnchantments(DataComponentPatch componentChanges,
                                         DataComponentType<ItemEnchantments> componentType,
                                         @Nullable DataComponentPredicate componentPredicate,
                                         Consumer<Component> loreAdder) {
        componentFromChanges(componentChanges, componentType).ifPresent(enchantments ->
                enchantments.entrySet().forEach(entry -> {
                    Holder<Enchantment> enchantmentType = entry.getKey();
                    int enchantmentLevel = entry.getIntValue();
                    // Name
                    loreAdder.accept(Component.empty().append(enchantmentType.value().description())
                                    .append(" ")
                                    .append(formatNumber(enchantmentLevel, enchantmentLevel)));
        }));
        if (componentPredicate instanceof EnchantmentsPredicateAccessor enchantmentsPredicate) {
            enchantmentsPredicate.ns0$getEnchantments().forEach(enchantmentPredicate -> {
                Optional<HolderSet<Enchantment>> enchantments = enchantmentPredicate.enchantments();
                MinMaxBounds.Ints levels = enchantmentPredicate.level();

                if (enchantments.isEmpty()) {
                    loreAdder.accept(Component.translatable("item_predicate.neospeedzero.extra_req.enchantments.any", formatIntRange(levels)));
                } else {
                    int size = enchantments.get().size();
                    if (size == 0) return;  // contains nothing

                    loreAdder.accept(Component.empty()
                            .append(enchantments.get().get(0).value().description())
                            .append(size == 1 ? " " : "... ")
                            .append(formatIntRange(levels))
                    );
                }
            });
        }
    }

    private static Component formatIntRange(MinMaxBounds.Ints intRange) {
        return formatNumber(intRange.min().orElse(null), intRange.max().orElse(null));
    }

    @Contract("null, null -> fail")
    private static Component formatNumber(@Nullable Integer min, @Nullable Integer max) {
        Preconditions.checkArgument(min != null || max != null);
        if (min == null) return Component.translatable("item_predicate.neospeedzero.extra_req.count.max", max);
        if (max == null) return Component.translatable("item_predicate.neospeedzero.extra_req.count.min", min);
        if (min.equals(max))
            return Component.translatable("item_predicate.neospeedzero.extra_req.count.exact", min);
        return Component.translatable("item_predicate.neospeedzero.extra_req.count.between", min, max);
    }

    private static <T> Optional<? extends T> componentFromChanges(DataComponentPatch changes, DataComponentType<? extends T> componentType) {
        Optional<? extends T> t = changes.get(componentType);
        if (t == null /*sic*/) return Optional.empty();
        return t;
    }
}
