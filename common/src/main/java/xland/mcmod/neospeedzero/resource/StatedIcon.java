package xland.mcmod.neospeedzero.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record StatedIcon(IconState iconState, ItemStackTemplate icon) {
    public static final Codec<StatedIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IconState.CODEC.lenientOptionalFieldOf("replace", IconState.COVERS_GEN).forGetter(StatedIcon::iconState),
            ItemStackTemplate.MAP_CODEC.forGetter(StatedIcon::icon)
    ).apply(instance, StatedIcon::new));

    public enum IconState implements BiFunction<ItemStackTemplate, ItemStackTemplate, ItemStack>, StringRepresentable {
        ICON("icon"),
        // icon first
        COVERS_GEN("covers_gen") {
            @Override
            public ItemStack apply(ItemStackTemplate icon, ItemStackTemplate gen) {
                return icon.apply(gen.components());
            }
        },
        // generated first
        COVERS_ICON("covers_icon") {
            @Override
            public ItemStack apply(ItemStackTemplate icon, ItemStackTemplate gen) {
                ItemStackTemplate template = new ItemStackTemplate(icon.item(), icon.count(), gen.components());
                return template.apply(icon.components());
            }
        },
        ;
        private final String id;

        IconState(String id) {
            this.id = id;
        }

        @Override
        public ItemStack apply(ItemStackTemplate icon, ItemStackTemplate gen) {
            return icon.create();
        }

        @Override
        public @NotNull String getSerializedName() {
            return id;
        }

        public static final StringRepresentable.EnumCodec<@NotNull IconState> CODEC = StringRepresentable.fromEnum(IconState::values);
    }
}
