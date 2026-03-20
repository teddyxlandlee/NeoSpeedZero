package xland.mcmod.neospeedzero.paper;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xland.mcmod.neospeedzero.NeoSpeedZero;

import java.util.function.Supplier;

public class NeoSpeedZeroPaper extends JavaPlugin {
    private static final NeoSpeedZeroPaper INSTANCE = new NeoSpeedZeroPaper();

    private NeoSpeedZeroPaper() {
    }

    static NeoSpeedZeroPaper getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        NeoSpeedZero.init();

        // Load goals
        PaperEvents.applyGoalsFrom(CraftBukkitReflections.getServer());

        // Server Start Events
        PaperEvents.SERVER_STARTING.invoker().accept(CraftBukkitReflections.getServer());

        // Commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            for (Supplier<? extends LiteralArgumentBuilder<CommandSourceStack>> supplier : PaperEvents.COMMANDS.invoker()) {
                registrar.register(supplier.get().build());
            }
        });

        // Player Tick Events
        final var playerTickTask = PaperEvents.getPlayerTickTask();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerTickTask.accept(player);
        }

        // Network
        var messenger = this.getServer().getMessenger();
        for (var k : PaperNetwork.getS2CKeys()) {
            messenger.registerOutgoingPluginChannel(this, k.toString());
        }
        PaperNetwork.C2S.forEach((key, consumer) -> messenger.registerIncomingPluginChannel(
                this, key.asString(), (_, player, _) -> consumer.accept(player)
        ));

        // Other event listeners that cannot be covered by Mixins
        this.getServer().getPluginManager().registerEvents(new NeoSpeedBukkitListener(), this);
    }

    @Override
    public void onDisable() {
        PaperEvents.SERVER_STOPPING.invoker().accept(CraftBukkitReflections.getServer());
    }
}
