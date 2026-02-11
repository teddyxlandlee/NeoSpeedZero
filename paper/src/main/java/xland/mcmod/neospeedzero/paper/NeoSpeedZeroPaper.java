package xland.mcmod.neospeedzero.paper;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xland.mcmod.neospeedzero.NeoSpeedZero;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeoSpeedZeroPaper extends JavaPlugin {
    private static final AtomicReference<NeoSpeedZeroPaper> INSTANCE = new AtomicReference<>();

    public NeoSpeedZeroPaper() {
        Validate.validState(INSTANCE.compareAndSet(null, this), "Duplicate NS0-Paper instance");
    }

    static NeoSpeedZeroPaper getInstance() {
        return INSTANCE.get();
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
        this.getServer().getGlobalRegionScheduler().runAtFixedRate(this, _ -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerTickTasks.computeIfAbsent(player, p -> schedulePlayerTickTask(p, playerTickTask));
            }
        }, 0L, 1L);

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

    private final WeakHashMap<Player, ScheduledTask> playerTickTasks = new WeakHashMap<>();

    private ScheduledTask schedulePlayerTickTask(Player player, Consumer<? super Player> task) {
        return player.getScheduler().runAtFixedRate(this, _ -> task.accept(player), null, 0L, 1L);
    }

    @Override
    public void onDisable() {
        PaperEvents.SERVER_STOPPING.invoker().accept(CraftBukkitReflections.getServer());
    }
}
