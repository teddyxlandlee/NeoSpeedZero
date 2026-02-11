package xland.mcmod.neospeedzero.paper;

import net.kyori.adventure.key.Key;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class CraftBukkitReflections {
    private CraftBukkitReflections() {}
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final MethodHandle MH_MinecraftServer_getServer;
    private static final MethodHandle MH_Identifier_asKey;
    private static final MethodHandle MH_BukkitPlayer_asVanilla;
    private static final MethodHandle MH_ServerPlayer_asBukkit;
    private static final MethodHandle MH_CraftRegistry_getMinecraftRegistry;
    private static final MethodHandle MH_BukkitAdvancement_asVanilla;
    private static final MethodHandle MH_ItemStack_asVanilla;

    public static MinecraftServer getServer() {
        try {
            return (MinecraftServer) MH_MinecraftServer_getServer.invokeExact();
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static Key asKey(Identifier id) {
        try {
            return (Key) MH_Identifier_asKey.invokeExact(id);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static org.bukkit.entity.Player asBukkitPlayer(ServerPlayer vanilla) {
        try {
            return (org.bukkit.entity.Player) MH_ServerPlayer_asBukkit.invokeExact(vanilla);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static net.minecraft.world.entity.player.Player asVanillaPlayer(org.bukkit.entity.Player bukkit) {
        return asVanillaServerPlayer(bukkit);
    }

    public static ServerPlayer asVanillaServerPlayer(org.bukkit.entity.Player bukkit) {
        try {
            return (ServerPlayer) MH_BukkitPlayer_asVanilla.invokeExact(bukkit);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static RegistryAccess getRegistryAccess() {
        try {
            return (RegistryAccess) MH_CraftRegistry_getMinecraftRegistry.invokeExact();
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static AdvancementHolder asVanillaAdvancement(org.bukkit.advancement.Advancement bukkit) {
        try {
            return (AdvancementHolder) MH_BukkitAdvancement_asVanilla.invokeExact(bukkit);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    public static net.minecraft.world.item.ItemStack asVanillaItemStack(org.bukkit.inventory.ItemStack bukkit) {
        try {
            return (net.minecraft.world.item.ItemStack) MH_ItemStack_asVanilla.invokeExact(bukkit);
        } catch (Throwable e) {
            throw sneakyThrow(e);
        }
    }

    static {
        /* MinecraftServer.getServer() */
        try {
            //noinspection JavaLangInvokeHandleSignature
            MH_MinecraftServer_getServer = LOOKUP.findStatic(MinecraftServer.class, "getServer", MethodType.methodType(MinecraftServer.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* Identifier as Key */
        try {
            var keyCreator = LOOKUP.findStatic(Key.class, "key", MethodType.methodType(Key.class, String.class, String.class));
            var namespaceGetter = LOOKUP.findVirtual(Identifier.class, "getNamespace", MethodType.methodType(String.class));
            var pathGetter = LOOKUP.findVirtual(Identifier.class, "getPath", MethodType.methodType(String.class));

            MethodHandle handle = keyCreator;
            handle = MethodHandles.filterArguments(handle, 1, pathGetter);
            handle = MethodHandles.filterArguments(handle, 0, namespaceGetter);
            handle = MethodHandles.permuteArguments(handle, MethodType.methodType(Key.class, Identifier.class), 0, 0);
            MH_Identifier_asKey = handle;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* org.bukkit.entity.Player -> vanilla Player */
        try {
            var handle = MethodHandles.identity(org.bukkit.entity.Player.class);
            var craftPlayerClass = LOOKUP.findClass("org.bukkit.craftbukkit.entity.CraftPlayer");
            var MH_getHandle = LOOKUP.unreflect(craftPlayerClass.getMethod("getHandle"));

            handle = handle.asType(handle.type().changeReturnType(craftPlayerClass));
            handle = MethodHandles.filterReturnValue(handle, MH_getHandle);
            handle = handle.asType(handle.type().changeReturnType(ServerPlayer.class));
            MH_BukkitPlayer_asVanilla = handle;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* ServerPlayer -> org.bukkit.entity.Player */
        try {
            //noinspection JavaReflectionMemberAccess
            var handle = LOOKUP.unreflect(ServerPlayer.class.getMethod("getBukkitEntity"));
            handle = handle.asType(handle.type().changeReturnType(org.bukkit.entity.Player.class));
            MH_ServerPlayer_asBukkit = handle;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* CraftRegistry.getMinecraftRegistry() */
        try {
            var craftRegistryClass = LOOKUP.findClass("org.bukkit.craftbukkit.CraftRegistry");
            var handle = LOOKUP.unreflect(craftRegistryClass.getMethod("getMinecraftRegistry"));
            handle = handle.asType(MethodType.methodType(RegistryAccess.class));
            MH_CraftRegistry_getMinecraftRegistry = handle;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* org.bukkit.advancement.Advancement -> AdvancementHolder */
        try {
            var handle = MethodHandles.identity(org.bukkit.advancement.Advancement.class);
            var craftAdvancementClass = LOOKUP.findClass("org.bukkit.craftbukkit.advancement.CraftAdvancement");

            var MH_getHandle = LOOKUP.unreflect(craftAdvancementClass.getMethod("getHandle"));

            handle = handle.asType(handle.type().changeReturnType(craftAdvancementClass));
            handle = MethodHandles.filterReturnValue(handle, MH_getHandle);
            handle = handle.asType(handle.type().changeReturnType(AdvancementHolder.class));
            MH_BukkitAdvancement_asVanilla = handle;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }

        /* org.bukkit.inventory.ItemStack -> net.minecraft.world.item.ItemStack */
        try {
            var craftItemStackClass = LOOKUP.findClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
            MH_ItemStack_asVanilla = LOOKUP.findStatic(craftItemStackClass, "unwrap", MethodType.methodType(net.minecraft.world.item.ItemStack.class, org.bukkit.inventory.ItemStack.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> Error sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
