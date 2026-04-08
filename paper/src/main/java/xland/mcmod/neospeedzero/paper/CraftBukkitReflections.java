package xland.mcmod.neospeedzero.paper;

import net.kyori.adventure.key.Key;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public final class CraftBukkitReflections {
    private CraftBukkitReflections() {}

    public static MinecraftServer getServer() {
        return MinecraftServer.getServer();
    }

    @SuppressWarnings("PatternValidation")
    public static Key asKey(Identifier id) {
        return Key.key(id.getNamespace(), id.getPath());
    }

    public static org.bukkit.entity.Player asBukkitPlayer(ServerPlayer vanilla) {
        return vanilla.getBukkitEntity();
    }

    public static net.minecraft.world.entity.player.Player asVanillaPlayer(org.bukkit.entity.Player bukkit) {
        return asVanillaServerPlayer(bukkit);
    }

    public static ServerPlayer asVanillaServerPlayer(org.bukkit.entity.Player bukkit) {
        return ((CraftPlayer) bukkit).getHandle();
    }

    public static RegistryAccess getRegistryAccess() {
        return CraftRegistry.getMinecraftRegistry();
    }

    public static AdvancementHolder asVanillaAdvancement(org.bukkit.advancement.Advancement bukkit) {
        return ((CraftAdvancement) bukkit).getHandle();
    }

    public static net.minecraft.world.item.ItemStack asVanillaItemStack(org.bukkit.inventory.ItemStack bukkit) {
        return CraftItemStack.unwrap(bukkit);
    }
}
