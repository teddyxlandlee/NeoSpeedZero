package xland.mcmod.neospeedzero.itemext;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.mixin.CustomDataAccessor;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public interface ItemExtensions {
    String TAG_MOD_GIVEN = NeoSpeedZero.MOD_ID + "_given_item";
    String TAG_INFINITE_FIREWORK = "infinite_firework";

    static ItemStack commonFireworks(UUID recordId) {
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 64);
        patchModGiven(itemStack, recordId);
        return itemStack;
    }

    static ItemStack infiniteFireworks(UUID recordId) {
        ItemStack itemStack = commonFireworks(recordId);
        CustomData.update(
                DataComponents.CUSTOM_DATA, itemStack,
                tag -> tag.putBoolean(TAG_INFINITE_FIREWORK, true)
        );
        return itemStack;
    }

    static ItemStack commonElytra(UUID recordId) {
        ItemStack itemStack = Items.ELYTRA.getDefaultInstance();
        patchModGiven(itemStack, recordId);
        return itemStack;
    }

    static ItemStack infiniteElytra(UUID recordId) {
        ItemStack itemStack = commonElytra(recordId);
        itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        return itemStack;
    }

    private static void patchModGiven(ItemStack stack, UUID recordId) {
        CustomData.update(
                DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putIntArray(TAG_MOD_GIVEN, UUIDUtil.uuidToIntArray(recordId))
        );
        stack.update(DataComponents.LORE, ItemLore.EMPTY, lore -> {
            // Append "Speedrunners only" tip
            return lore.withLineAdded(Component.translatable("message.neospeedzero.item.mod_given"));
        });
    }

    static boolean matchesRecordId(ItemStack stack, @Nullable UUID recordId) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return true;    // Not marked

        // we just read, not write
        CompoundTag rawTag = cast(customData).ns0$getUnsafe();
        Optional<int[]> intArray = rawTag.getIntArray(TAG_MOD_GIVEN);
        return intArray.map(
                        // If marked, then a record must be present and matched.
                    ints -> recordId != null && Arrays.equals(ints, UUIDUtil.uuidToIntArray(recordId))
                )
                .orElse(true);  // Not marked
    }

    static boolean isModGivenItem(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;

        return cast(customData).ns0$getUnsafe().contains(TAG_MOD_GIVEN);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isInfiniteFirework(ItemStack stack) {
        if (!stack.is(Items.FIREWORK_ROCKET)) return false;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;

        // we just read, not write
        CompoundTag rawTag = cast(customData).ns0$getUnsafe();
        return rawTag.getBooleanOr(TAG_INFINITE_FIREWORK, false);
    }

    static void give(@NotNull ServerPlayer player, @NotNull ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, true);
        }
    }

    private static CustomDataAccessor cast(CustomData value) {
        return (CustomDataAccessor) (Object) value;
    }
}
