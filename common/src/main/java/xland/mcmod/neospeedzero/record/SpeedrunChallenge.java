package xland.mcmod.neospeedzero.record;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.neospeedzero.resource.StatedIcon;

import java.util.Optional;

@org.jspecify.annotations.NullMarked
public record SpeedrunChallenge(Either<ItemPredicate, ResourceKey<Advancement>> challenge, ItemStack icon) {
    public static final Codec<SpeedrunChallenge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Type.CODEC.dispatch(Type::fromEither, Type::toMapCodec).fieldOf("challenge").forGetter(SpeedrunChallenge::challenge),
        ItemStack.CODEC.fieldOf("icon").forGetter(SpeedrunChallenge::icon)
    ).apply(instance, SpeedrunChallenge::new));

    public static SpeedrunChallenge of(Either<ItemPredicate, ResourceKey<Advancement>> challenge, ItemStack generatedIcon, @Nullable StatedIcon statedIcon) {
        if (statedIcon == null) return new SpeedrunChallenge(challenge, generatedIcon);

        ItemStack statedIconStack = statedIcon.icon().copy();
        statedIcon.iconState().accept(statedIconStack, generatedIcon);

        return new SpeedrunChallenge(challenge, statedIconStack);
    }

    public static SpeedrunChallenge of(Either<ItemPredicate, ResourceKey<Advancement>> challenge, ItemStack generatedIcon, Optional<StatedIcon> statedIcon) {
        return of(challenge, generatedIcon, statedIcon.orElse(null));
    }

    private enum Type implements StringRepresentable {
        ITEM_PREDICATE("item"),
        ADVANCEMENT("advancement")
        ;
        private final String id;

        Type(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        private static Type fromEither(Either<ItemPredicate, ResourceKey<Advancement>> either) {
            return either.left().isPresent() ? ITEM_PREDICATE : ADVANCEMENT;
        }

        private MapCodec<Either<ItemPredicate, ResourceKey<Advancement>>> toMapCodec() {
            return switch (this) {
                case ITEM_PREDICATE -> ItemPredicate.CODEC.<Either<ItemPredicate, ResourceKey<Advancement>>>flatComapMap(
                        Either::left,
                        either -> either.map(DataResult::success, r -> errorResult())
                ).fieldOf("item");
                case ADVANCEMENT -> ResourceKey.codec(Registries.ADVANCEMENT).<Either<ItemPredicate, ResourceKey<Advancement>>>flatComapMap(
                        Either::right,
                        either -> either.map(l -> errorResult(), DataResult::success)
                ).fieldOf("advancement");
            };
        }

        private <T> DataResult<T> errorResult() {
            return DataResult.error(() -> "Expected " + getSerializedName() + ", got another");
        }

        private static final StringRepresentable.EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
    }
}
