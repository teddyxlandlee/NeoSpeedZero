package xland.mcmod.neospeedzero.util;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Objects;

@NotNullByDefault
public abstract sealed class TranslatableComponentFactory implements java.io.Serializable {
    private final String key;
    private final String fallback;

    public static NoArgs noArgs(String key, String fallback) {
        return new NoArgs(key, fallback);
    }

    public static WithArgs withArgs(String key, String fallback) {
        return new WithArgs(key, fallback);
    }

    public static final class NoArgs extends TranslatableComponentFactory {
        private NoArgs(String key, String fallback) {
            super(key, fallback);
        }

        public Component create() {
            return Component.translatableWithFallback(getKey(), getFallback());
        }
    }

    public static final class WithArgs extends TranslatableComponentFactory {
        private WithArgs(String key, String fallback) {
            super(key, fallback);
        }

        private static Object mapArgument(@UnknownNullability Object arg) {
            // See TranslatableContents.isAllowedPrimitiveArgument()
            return switch (arg) {
                case String _, Number _, Boolean _ -> arg;
                case null, default -> String.valueOf(arg);
            };
        }

        public Component createWithArgs(@UnknownNullability Object... args) {
            args = Arrays.stream(args).map(WithArgs::mapArgument).toArray();
            // runtime type: Object[]
            return Component.translatableWithFallback(getKey(), getFallback(), args);
        }
    }

    private TranslatableComponentFactory(String key, String fallback) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(fallback, "fallback");
        this.key = key;
        this.fallback = fallback;
    }

    // Utilities
    public final String getKey() {
        return key;
    }

    public final String getFallback() {
        return fallback;
    }

    // Serial
    private record SerProxy(String key, String fallback, boolean withArgs) implements java.io.Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        @java.io.Serial
        private Object readResolve() {
            return withArgs ? TranslatableComponentFactory.withArgs(key, fallback) : TranslatableComponentFactory.noArgs(key, fallback);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @java.io.Serial
    private Object writeReplace() {
        return new SerProxy(key, fallback, this instanceof WithArgs);
    }
}
