package xland.mcmod.neospeedzero.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public record StatedIcon(IconState iconState, ItemStack icon) {
    public static final Codec<StatedIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IconState.CODEC.lenientOptionalFieldOf("replace", IconState.COVERS_GEN).forGetter(StatedIcon::iconState),
            ItemStack.MAP_CODEC.forGetter(StatedIcon::icon)
    ).apply(instance, StatedIcon::new));

    public enum IconState implements BiConsumer<ItemStack, ItemStack>, StringRepresentable {
        ICON("icon", (icon, gen) -> {}),
        // icon first
        COVERS_GEN("covers_gen", (icon, gen) -> icon.applyComponents(DataComponentMap.composite(/*alternative=*/gen.getComponents(), /*primary=*/icon.getComponents()))),
        // generated first
        COVERS_ICON("covers_icon", (icon, gen) -> icon.applyComponents(DataComponentMap.composite(/*alternative=*/icon.getComponents(), /*primary=*/gen.getComponents()))),
        ;
        private final String id;
        private final BiConsumer<ItemStack, ItemStack> wrappedConsumer;

        IconState(String id, BiConsumer<ItemStack, ItemStack> wrappedConsumer) {
            this.id = id;
            this.wrappedConsumer = wrappedConsumer;
        }

        @Override
        public void accept(ItemStack icon, ItemStack gen) {
            wrappedConsumer.accept(icon, gen);
        }

        @Override
        public @NotNull String getSerializedName() {
            return id;
        }

        public static final StringRepresentable.EnumCodec<IconState> CODEC = StringRepresentable.fromEnum(IconState::values);
    }
}
