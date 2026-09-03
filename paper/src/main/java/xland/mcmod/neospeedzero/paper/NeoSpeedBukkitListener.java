package xland.mcmod.neospeedzero.paper;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.itemext.ItemExtensions;

public final class NeoSpeedBukkitListener implements Listener {
    NeoSpeedBukkitListener() {}

    @EventHandler   // substitutes PlayerAdvancementsMixin
    public void onAdvancementMade(PlayerAdvancementDoneEvent event) {
        NeoSpeedLifecycle.onAdvancementMade(
                CraftBukkitConversions.asVanillaServerPlayer(event.getPlayer()),
                CraftBukkitConversions.asVanillaAdvancement(event.getAdvancement())
        );
    }

    @EventHandler   // functionally substitutes InventoryChangeTriggerMixin
    public void onInventoryChange(PlayerInventorySlotChangeEvent event) {
        NeoSpeedLifecycle.onInventoryChange(
                CraftBukkitConversions.asVanillaServerPlayer(event.getPlayer()),
                CraftBukkitConversions.asVanillaItemStack(event.getNewItemStack())
        );
    }

    @EventHandler   // substitutes FireworkRocketItemMixin.stopConsuming
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        var vanillaItemStack = CraftBukkitConversions.asVanillaItemStack(event.getItemStack());
        if (ItemExtensions.isInfiniteFirework(vanillaItemStack)) {
            event.setShouldConsume(false);
        }
    }

    @EventHandler   // substitutes FireworkRocketItemMixin.stopShrinking
    public void onFireworkLaunch(PlayerLaunchProjectileEvent event) {
        var vanillaItemStack = CraftBukkitConversions.asVanillaItemStack(event.getItemStack());
        if (ItemExtensions.isInfiniteFirework(vanillaItemStack)) {
            event.setShouldConsume(false);
        }
    }

    @EventHandler   // reload goal holders (not including first load)
    public void onResourceReload(ServerResourcesReloadedEvent event) {
        PaperEvents.applyGoalsFrom(CraftBukkitConversions.getServer());
    }
}
