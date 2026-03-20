package xland.mcmod.neospeedzero.paper;

import com.google.common.base.Preconditions;
import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class NeoSpeedZeroPaperBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, (RegistrarEvent<DatapackRegistrar> event) -> {
            try {
                // Unlike mod platforms, Paper does not treat plugins as datapack.
                // Here we register the plugin source (most likely, the JAR) as the plugin source.
                // However, Paper's PackDetector only recognizes folders and zips. If referenced file
                // is symlink, it follows the link.
                // Therefore, we have to copy the jar to a temporary place.

                // 0. If the plugin source is not a JAR, we don't need a copy anymore
                final Path pluginSource = context.getPluginSource();
                final Path packCopy;
                if (Files.isDirectory(pluginSource) || pluginSource.getFileName().endsWith(".zip")) {
                    packCopy = pluginSource;
                } else {
                    // 1. Check version. Identical implementation version is accepted.
                    final var myVersion = NeoSpeedZeroPaperBootstrap.class.getPackage().getImplementationVersion();
                    Preconditions.checkState(myVersion != null, "Running a corrupted NeoSpeedZero" +
                            " plugin without Implementation-Version attribute or valid manifest");

                    packCopy = Files.createDirectories(context.getDataDirectory()).resolve(".ns0-builtin-pack_copy.zip");
                    String packCopyVersion = null;

                    if (Files.isRegularFile(packCopy)) {
                        try (var jis = new JarInputStream(Files.newInputStream(packCopy))) {
                            packCopyVersion = jis.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                        }
                    }
                    org.slf4j.LoggerFactory.getLogger(NeoSpeedZeroPaperBootstrap.class).info("packCopyVersion = {}", myVersion);
                    if (!Objects.equals(myVersion, packCopyVersion)) {
                        // 2. If version unmatch (or file does not exist), copy it
                        Files.copy(context.getPluginSource(), packCopy, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                event.registrar().discoverPack(
                        packCopy, "PaperPluginBuiltin", c -> c
                                .autoEnableOnServerStart(true)
                                .title(Component.text("NS0 Builtin Pack"))
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to load NS0 builtin datapack", e);
            }
        });
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return NeoSpeedZeroPaper.getInstance();
    }
}
