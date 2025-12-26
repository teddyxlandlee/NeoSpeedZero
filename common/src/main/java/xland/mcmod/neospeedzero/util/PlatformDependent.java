package xland.mcmod.neospeedzero.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformDependent {
    enum Platform {
        FABRIC("net.fabricmc.loader.api.FabricLoader"),
        FORGE("net.minecraftforge.versions.forge.ForgeVersion"),
        NEO("net.neoforged.fml.ModLoader"),
        ;
        private final String declaredClass;

        Platform(String declaredClass) {
            this.declaredClass = declaredClass;
        }

        private boolean isCurrentPlatform() {
            var lookup = MethodHandles.lookup();
            try {
                lookup.findClass(declaredClass);
                return true;
            } catch (ClassNotFoundException | IllegalAccessException e) {
                if (e instanceof IllegalAccessException) {
                    org.slf4j.LoggerFactory.getLogger(Platform.class).error(
                            "Inaccessible class {} found when probing for platform {}",
                            declaredClass, this, e
                    );
                }
                return false;
            }
        }

        private static Platform CURRENT_PLATFORM;

        public static Platform getCurrentPlatform() {
            if (CURRENT_PLATFORM == null) {
                CURRENT_PLATFORM = Arrays.stream(values())
                        .filter(Platform::isCurrentPlatform)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Unknown platform"));
            }
            return CURRENT_PLATFORM;
        }

        /// Recommended to be cached:
        /// ```java
        /// public static XxxApi getInstance() {
        ///     class Holder {
        ///         static final XxxApi INSTANCE = PlatformDependent.Platform.probe(XxxApi.class);
        ///     }
        ///     return Holder.INSTANCE;
        /// }
        /// ```
        public static <T> T probe(Class<T> klass) {
            final Platform currentPlatform = getCurrentPlatform();
            final List<ServiceLoader.Provider<T>> providers = ServiceLoader.load(klass)
                    .stream()
                    .filter(provider -> {
                        final Class<? extends T> implClass = provider.type();
                        final PlatformDependent annotation = implClass.getDeclaredAnnotation(PlatformDependent.class);
                        return annotation != null && currentPlatform == annotation.value();
                    })
                    .toList();
            switch (providers.size()) {
                case 0 -> throw new IllegalStateException("Cannot find service " + klass + " under platform " + currentPlatform);
                case 1 -> {}
                default -> throw new IllegalStateException("Found multiple implementations of service" + klass + " under platform " + currentPlatform + ": " + providers);
            }
            return providers.getFirst().get();
        }
    }

    Platform value();
}
