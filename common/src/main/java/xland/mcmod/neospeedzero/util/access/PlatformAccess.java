package xland.mcmod.neospeedzero.util.access;

import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.component.CustomData;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedServer;

import java.util.function.Function;
import java.util.function.Supplier;

public final class PlatformAccess {
    private PlatformAccess() {}

    private static final Function<MinecraftServer, NeoSpeedServer> A_NeoSpeedServer = castOr(
            MinecraftServer.class, NeoSpeedServer.class,
            () -> new PlatformWrapper<>(PlatformWrapper.NeoSpeedServerImpl::new)
    );
    public static NeoSpeedServer wrap(MinecraftServer server) { return A_NeoSpeedServer.apply(server); }

    private static final Function<PlayerAdvancements, AdvancementProgressGetter> A_AdvancementProgressGetter = castOr(
            PlayerAdvancements.class, AdvancementProgressGetter.class,
            () -> PlatformWrapper.AdvancementProgressGetterImpl::new
    );
    public static AdvancementProgressGetter wrap(PlayerAdvancements playerAdvancements) { return A_AdvancementProgressGetter.apply(playerAdvancements); }

    private static final Function<EnchantmentsPredicate, EnchantmentPredicateListProvider> A_EnchantmentPredicateListProvider = castOr(
            EnchantmentsPredicate.class, EnchantmentPredicateListProvider.class,
            () -> PlatformWrapper.EnchantmentPredicateListProviderImpl::new
    );
    public static EnchantmentPredicateListProvider wrap(EnchantmentsPredicate predicate) { return A_EnchantmentPredicateListProvider.apply(predicate); }

    private static final Function<CustomData, CustomDataTagProvider> A_CustomDataTagProvider = castOr(
            CustomData.class, CustomDataTagProvider.class,
            () -> PlatformWrapper.CustomDataTagProviderImpl::new
    );
    public static CustomDataTagProvider wrap(CustomData component) { return A_CustomDataTagProvider.apply(component); }

    private static <V, A> Function<V, A> castOr(Class<V> vanillaClass, Class<A> accessorClass,
                                                Supplier<Function<V, A>> alternative) {
        if (accessorClass.isAssignableFrom(vanillaClass)) {
            return accessorClass::cast;
        } else {
            return alternative.get();
        }
    }
}
