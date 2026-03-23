package xland.mcmod.neospeedzero.resource;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.NeoSpeedTranslations;
import xland.mcmod.neospeedzero.util.access.EnchantmentPredicateListProvider;
import xland.mcmod.neospeedzero.util.access.PlatformAccess;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.jspecify.annotations.NullMarked
final class ExtraRequirements {
    static ItemStackTemplate fillExtraRequirements(ItemStackTemplate template,
                                                   @Nullable Either<TagKey<Item>, HolderSet<Item>> ofAny,
                                                   @Nullable ItemPredicate predicate
    ) {
        List<Component> appendedLores = new ArrayList<>();
        ItemStack instance = template.create();

        if (predicate != null) {
            // Count
            MinMaxBounds.Ints count = predicate.count();
            if (!count.isAny())
                appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_COUNT.createWithArgs(formatIntRange(count)));

            // Component
            final DataComponentPatch componentChanges = predicate.components().exact().asPatch();
            final Map<DataComponentPredicate.Type<?>, DataComponentPredicate> partial = predicate.components().partial();
            instance.applyComponents(componentChanges);

            // Damage - [E,P]minecraft:damage
            instance.remove(DataComponents.DAMAGE);
            componentFromChanges(instance, DataComponents.DAMAGE).ifPresentOrElse(damage -> {
                // Exact damage predicate
                appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_DAMAGE.createWithArgs(formatNumber(damage, damage)));
            }, () -> {
                if (partial.get(DataComponentPredicates.DAMAGE) instanceof DamagePredicate(
                        MinMaxBounds.Ints durability, MinMaxBounds.Ints damage
                )) {
                    appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_DURABILITY.createWithArgs(formatIntRange(durability)));
                    appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_DAMAGE.createWithArgs(formatIntRange(damage)));
                }
            });
            // Enchantments - [E,P]minecraft:stored_enchantments, minecraft:enchantments
            readEnchantments(partial.get(DataComponentPredicates.STORED_ENCHANTMENTS), appendedLores::add);
            readEnchantments(partial.get(DataComponentPredicates.ENCHANTMENTS), appendedLores::add);

            // Bundle Contents - [P]minecraft:bundle_contents
            // [E]minecraft:bundle_contents will be handled by ItemStack.components
            if (partial.get(DataComponentPredicates.BUNDLE_CONTENTS) instanceof BundlePredicate(
                    Optional<CollectionPredicate<ItemInstance, ItemPredicate>> items
            ) && items.isPresent()) {
                // Too complicated
                // Similar for [P]minecraft:container
                appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_BUNDLE.create());
            }

            // Custom Data - [E,P]minecraft:custom_data
            if (partial.containsKey(DataComponentPredicates.CUSTOM_DATA) || componentFromChanges(instance, DataComponents.CUSTOM_DATA).isPresent()) {
                appendedLores.add(NeoSpeedTranslations.EXTRA_REQUIREMENTS_CUSTOM_DATA.create());
            }

            // TODO: [E,P] firework_explosion, fireworks, writable_book_contents, written_book_contents

            // Music - minecraft:jukebox_playable
            if (partial.get(DataComponentPredicates.JUKEBOX_PLAYABLE) instanceof JukeboxPlayablePredicate(
                    Optional<HolderSet<JukeboxSong>> song
            ) && song.isPresent()) {
                appendHomogenousSet(
                        NeoSpeedTranslations.EXTRA_REQUIREMENTS_SONG.create(),
                        song.get(),
                        appendedLores::add
                );
            }

            if (partial.get(DataComponentPredicates.POTIONS) instanceof PotionsPredicate(HolderSet<Potion> potions)) {
                appendHomogenousSet(NeoSpeedTranslations.EXTRA_REQUIREMENTS_POTION.create(), potions, appendedLores::add);
            }

            if (partial.get(DataComponentPredicates.ARMOR_TRIM) instanceof TrimPredicate(
                    Optional<HolderSet<TrimMaterial>> material, Optional<HolderSet<TrimPattern>> pattern
            )) {
                material.ifPresent(holders -> appendHomogenousSet(
                        NeoSpeedTranslations.EXTRA_REQUIREMENTS_TRIM_MATERIAL.create(), holders, appendedLores::add)
                );
                pattern.ifPresent(holders -> appendHomogenousSet(
                        NeoSpeedTranslations.EXTRA_REQUIREMENTS_TRIM_PATTERNS.create(), holders, appendedLores::add
                ));
            }
        }

        if (ofAny != null) {
            appendHomogenousSet(NeoSpeedTranslations.EXTRA_REQUIREMENTS_ITEMS.create(), ofAny, appendedLores::add);
        }

        if (!appendedLores.isEmpty()) {
            List<Component> lines = Optional.ofNullable(instance.get(DataComponents.LORE))
                    .map(ItemLore::lines)
                    .orElse(Collections.emptyList());
            lines = new ArrayList<>(lines);
            lines.addAll(appendedLores);
            instance.set(DataComponents.LORE, new ItemLore(lines));
        }
        return ItemStackTemplate.fromNonEmptyStack(instance);
    }

    private static void readEnchantments(@Nullable DataComponentPredicate componentPredicate,
                                         Consumer<Component> loreAdder) {
        if (componentPredicate instanceof EnchantmentsPredicate predicate) {
            EnchantmentPredicateListProvider enchantmentsPredicate = PlatformAccess.wrap(predicate);
            enchantmentsPredicate.ns0$getEnchantments().forEach(enchantmentPredicate -> {
                Optional<HolderSet<Enchantment>> enchantments = enchantmentPredicate.enchantments();
                MinMaxBounds.Ints levels = enchantmentPredicate.level();

                if (enchantments.isEmpty()) {
                    loreAdder.accept(NeoSpeedTranslations.EXTRA_REQUIREMENTS_ENCHANTMENTS_ANY.createWithArgs(formatIntRange(levels)));
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
        if (min == null) return NeoSpeedTranslations.EXTRA_REQUIREMENTS_COUNT_MAX.createWithArgs(max);
        if (max == null) return NeoSpeedTranslations.EXTRA_REQUIREMENTS_COUNT_MIN.createWithArgs(min);
        if (min.equals(max))
            return NeoSpeedTranslations.EXTRA_REQUIREMENTS_COUNT_EXACT.createWithArgs(min);
        return NeoSpeedTranslations.EXTRA_REQUIREMENTS_COUNT_BETWEEN.createWithArgs(min, max);
    }

    private static <T> Optional<? extends T> componentFromChanges(DataComponentGetter changes, DataComponentType<? extends T> componentType) {
        return Optional.ofNullable(changes.get(componentType));
    }

    private static <T> Stream<String> formatHomogenousSet(Either<TagKey<T>, HolderSet<T>> unwrapped) {
        return unwrapped.map(
                tagKey -> Stream.of("#" + tagKey.location()),
                holders -> holders.stream().map(Holder::getRegisteredName)
        );
    }

    private static <T> void appendHomogenousSet(
            MutableComponent prefix, Either<TagKey<T>, HolderSet<T>> unwrapped,
            Consumer<Component> loreAdder
    ) {
        final String text = formatHomogenousSet(unwrapped).map(s -> "\n - " + s).collect(Collectors.joining());
        loreAdder.accept(prefix.append(text));
    }

    private static <T> void appendHomogenousSet(
            MutableComponent prefix, HolderSet<T> wrapped, Consumer<Component> loreAdder
    ) {
        appendHomogenousSet(prefix, wrapped.unwrap().mapRight(HolderSet::direct), loreAdder);
    }
}
