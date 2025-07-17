package xland.mcmod.neospeedzero.difficulty;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.itemext.ItemExtensions;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public enum BuiltinDifficulty implements SpeedrunDifficulty {
    NN(GiveItem.NONE, GiveItem.NONE, "empty"),
    NC(GiveItem.NONE, GiveItem.COMMON, "elytra"),
    NU(GiveItem.NONE, GiveItem.UNBREAKABLE, "inf_elytra"),
    CN(GiveItem.COMMON, GiveItem.NONE, "firework"),
    CC(GiveItem.COMMON, GiveItem.COMMON, "firework_elytra"),
    CU(GiveItem.COMMON, GiveItem.UNBREAKABLE, "firework_inf_elytra"),
    UN(GiveItem.UNBREAKABLE, GiveItem.NONE, "inf_firework"),
    UC(GiveItem.UNBREAKABLE, GiveItem.COMMON, "inf_firework_elytra"),
    UU(GiveItem.UNBREAKABLE, GiveItem.UNBREAKABLE, "inf_firework_inf_elytra")
    ;
    private final String rawId;
    private final GiveItem forFirework, forElytra;

    BuiltinDifficulty(GiveItem forFirework, GiveItem forElytra, String rawId) {
        this.rawId = rawId;
        this.forFirework = forFirework;
        this.forElytra = forElytra;
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(NeoSpeedZero.MOD_ID, rawId);
    }

    @Override
    public @NotNull Component displayedName() {
        return Component.translatable("message.neospeedzero.difficulty." + rawId);
    }

    @Override
    public void onStart(ServerPlayer player, SpeedrunRecord record) {
        // Just give them, since these items vanishes when they stop running
        giveItems(player, record);
    }

    private void giveItems(ServerPlayer player, SpeedrunRecord record) {
        ItemExtensions.give(player, forFirework.createFirework(record.recordId()));
        ItemExtensions.give(player, forElytra.createElytra(record.recordId()));
    }

    private enum GiveItem {
        NONE(recordId -> null, recordId -> null),
        COMMON(ItemExtensions::commonFireworks, ItemExtensions::commonElytra),
        UNBREAKABLE(ItemExtensions::infiniteFireworks, ItemExtensions::infiniteElytra),
        ;
        
        private final Function<UUID, @Nullable ItemStack> fireworkFactory, elytraFactory;

        GiveItem(Function<UUID, @Nullable ItemStack> fireworkFactory, Function<UUID, @Nullable ItemStack> elytraFactory) {
            this.fireworkFactory = fireworkFactory;
            this.elytraFactory = elytraFactory;
        }


        @NotNull ItemStack createFirework(UUID recordId) {
            return Objects.requireNonNullElse(fireworkFactory.apply(recordId), ItemStack.EMPTY);
        }

        @NotNull ItemStack createElytra(UUID recordId) {
            return Objects.requireNonNullElse(elytraFactory.apply(recordId), ItemStack.EMPTY);
        }
    }
}
