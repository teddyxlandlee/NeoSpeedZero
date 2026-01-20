package xland.mcmod.neospeedzero.mixin;

import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xland.mcmod.neospeedzero.util.access.EnchantmentPredicateListProvider;

import java.util.List;

@Mixin(EnchantmentsPredicate.class)
interface EnchantmentsPredicateAccessor extends EnchantmentPredicateListProvider {
    @Invoker("enchantments")
    @Override
    List<EnchantmentPredicate> ns0$getEnchantments();
}
