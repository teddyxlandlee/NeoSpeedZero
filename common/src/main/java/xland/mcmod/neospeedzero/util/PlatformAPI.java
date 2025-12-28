package xland.mcmod.neospeedzero.util;

import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;
import xland.mcmod.neospeedzero.util.network.PlatformNetwork;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public abstract class PlatformAPI {
    // From DurationLocalizer$LangPatchProber
    public abstract boolean isLangPatchAvailable();

    public abstract PlatformEvents events();

    public abstract PlatformNetwork network();

    protected PlatformAPI() {
        Objects.requireNonNull(
                this.getClass().getDeclaredAnnotation(Implementation.class),
                "Implementations of PlatformAPI must be annotated with @PlatformAPI.Implementation"
        );
    }

    private static volatile PlatformAPI instance;

    public static PlatformAPI getInstance() {
        if (instance == null) {
            synchronized (PlatformAPI.class) {
                if (instance == null) {
                    instance = probe();
                }
            }
        }
        return instance;
    }


    private static PlatformAPI probe() {
        final Platform currentPlatform = Platform.detect();
        final List<ServiceLoader.Provider<PlatformAPI>> providers = ServiceLoader.load(PlatformAPI.class)
                .stream()
                .filter(provider -> {
                    final Class<? extends PlatformAPI> implClass = provider.type();
                    final Implementation annotation = implClass.getDeclaredAnnotation(Implementation.class);
                    return annotation != null && currentPlatform == annotation.value();
                })
                .toList();
        switch (providers.size()) {
            case 0 -> throw new IllegalStateException(
                    "Cannot find service PlatformAPI under platform " + currentPlatform
            );
            case 1 -> {}
            default -> throw new IllegalStateException(
                    "Found multiple implementations of service PlatformAPI under platform " + currentPlatform + ": " + providers
            );
        }
        return providers.getFirst().get();
    }

    public enum Platform {
        FABRIC("net.fabricmc.loader.api.FabricLoader"),
        FORGE("net.minecraftforge.versions.forge.ForgeVersion"),
        NEO("net.neoforged.fml.ModLoader"),
        ;
        private final String declaredClass;

        Platform(String declaredClass) {
            this.declaredClass = declaredClass;
        }

        private static Platform detected;

        public static @NotNull Platform detect() {
            if (detected == null) {
                var lookup = MethodHandles.lookup();
                Platform detected0 = null;
                for (Platform platform : values()) {
                    try {
                        lookup.findClass(platform.declaredClass);
                    } catch (ClassNotFoundException | IllegalAccessException e) {
                        if (e instanceof IllegalAccessException) {
                            org.slf4j.LoggerFactory.getLogger(Platform.class).error(
                                    "Inaccessible class {} found when probing for platform {}",
                                    platform.declaredClass, platform, e
                            );
                        }
                        continue;
                    }
                    detected0 = platform;
                    break;
                }
                if (detected0 == null) {
                    throw new IllegalStateException("Unknown platform");
                }
                detected = detected0;
            }

            return detected;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Implementation {
        Platform value();
    }
}
