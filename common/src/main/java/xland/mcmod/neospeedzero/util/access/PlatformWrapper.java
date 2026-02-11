package xland.mcmod.neospeedzero.util.access;

import com.google.common.base.Preconditions;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.component.CustomData;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedServer;
import xland.mcmod.neospeedzero.record.manager.RecordManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

final class PlatformWrapper<T, R> implements Function<T, R> {
    private final WeakHashMap<T, R> map = new WeakHashMap<>();
    private final Function<? super T, ? extends R> constructor;

    PlatformWrapper(Function<? super T, ? extends R> constructor) {
        this.constructor = constructor;
    }

    @Override
    public R apply(T t) {
        return map.computeIfAbsent(t, constructor);
    }

    @SuppressWarnings("unchecked")  // Throwable -> T
    private static <T extends Throwable> Error sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    private static void trySetAccessible(AccessibleObject member) throws IllegalStateException {
        Preconditions.checkState(member.trySetAccessible(), "Cannot make %s accessible", member);
    }

    // Must cache
    static final class NeoSpeedServerImpl implements NeoSpeedServer {
        private final RecordManager recordManager;

        @Override
        public RecordManager ns0$recordManager() {
            return recordManager;
        }

        NeoSpeedServerImpl(MinecraftServer server) {
            recordManager = new RecordManager(server);
        }
    }

    // These classes below are merely pre-computed wrappers. Not worth caching.

    static final class AdvancementProgressGetterImpl implements AdvancementProgressGetter {
        private final PlayerAdvancements self;
        private static final MethodHandle MH_progress;

        AdvancementProgressGetterImpl(PlayerAdvancements self) {
            this.self = self;
        }

        static {
            var lookup = MethodHandles.lookup();

            Field f;
            try {
                f = PlayerAdvancements.class.getDeclaredField("progress");
                trySetAccessible(f);
                MH_progress = lookup.unreflectGetter(f);
            } catch (ReflectiveOperationException e) {
                throw sneakyThrow(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<AdvancementHolder, AdvancementProgress> ns0$progress() {
            try {
                return (Map<AdvancementHolder, AdvancementProgress>) MH_progress.invoke(self);
            } catch (Throwable e) {
                throw sneakyThrow(e);
            }
        }
    }

    static final class EnchantmentPredicateListProviderImpl implements EnchantmentPredicateListProvider {
        private final EnchantmentsPredicate self;
        private static final MethodHandle MH_enchantments;

        EnchantmentPredicateListProviderImpl(EnchantmentsPredicate self) {
            this.self = self;
        }

        static {
            var lookup = MethodHandles.lookup();

            Method m;
            try {
                m = EnchantmentsPredicate.class.getDeclaredMethod("enchantments");
                trySetAccessible(m);
                MH_enchantments = lookup.unreflect(m);
            } catch (ReflectiveOperationException e) {
                throw sneakyThrow(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<EnchantmentPredicate> ns0$getEnchantments() {
            try {
                return (List<EnchantmentPredicate>) MH_enchantments.invoke(self);
            } catch (Throwable e) {
                throw sneakyThrow(e);
            }
        }
    }

    static final class CustomDataTagProviderImpl implements CustomDataTagProvider {
        private final CustomData self;
        private static final MethodHandle MH_getUnsafe;

        CustomDataTagProviderImpl(CustomData self) {
            this.self = self;
        }

        static {
            var lookup = MethodHandles.lookup();

            MethodHandle mh = null;
            try {
                // CustomData.getUnsafe() implemented by PaperMC
                //noinspection JavaLangInvokeHandleSignature
                mh = lookup.findVirtual(CustomData.class, "getUnsafe", MethodType.methodType(CompoundTag.class));
            } catch (NoSuchMethodException | IllegalAccessException _) {
            }
            if (mh == null) {
                Field f;
                try {
                    f = CustomData.class.getDeclaredField("tag");
                    trySetAccessible(f);
                    mh = lookup.unreflectGetter(f);
                } catch (ReflectiveOperationException e) {
                    throw sneakyThrow(e);
                }
            }
            MH_getUnsafe = mh;
        }

        @Override
        public CompoundTag ns0$getUnsafe() {
            try {
                return (CompoundTag) MH_getUnsafe.invoke(self);
            } catch (Throwable e) {
                throw sneakyThrow(e);
            }
        }
    }
}
