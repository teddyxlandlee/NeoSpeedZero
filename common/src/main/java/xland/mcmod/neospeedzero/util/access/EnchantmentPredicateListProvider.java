package xland.mcmod.neospeedzero.util.access;

import net.minecraft.advancements.predicates.EnchantmentPredicate;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public interface EnchantmentPredicateListProvider {
    List<EnchantmentPredicate> ns0$getEnchantments();
}
