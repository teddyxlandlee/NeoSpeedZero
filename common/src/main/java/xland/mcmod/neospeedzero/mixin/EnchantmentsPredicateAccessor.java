package xland.mcmod.neospeedzero.mixin;

import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xland.mcmod.neospeedzero.util.access.EnchantmentPredicateListProvider;

import java.util.List;

@Mixin(EnchantmentsPredicate.class)
interface EnchantmentsPredicateAccessor extends EnchantmentPredicateListProvider {
    @Invoker("enchantments")
    @Override
    @NullMarked
    List<EnchantmentPredicate> ns0$getEnchantments();
}
