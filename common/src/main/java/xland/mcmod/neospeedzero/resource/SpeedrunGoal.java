package xland.mcmod.neospeedzero.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.difficulty.SpeedrunDifficulty;
import xland.mcmod.neospeedzero.util.DialogUtil;

import java.util.*;

public record SpeedrunGoal(ItemStack icon, Component display, List<GoalPredicate> predicates) {
    public static final Codec<SpeedrunGoal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("icon").forGetter(SpeedrunGoal::icon),
            ComponentSerialization.CODEC.fieldOf("display").forGetter(SpeedrunGoal::display),
            GoalPredicate.CODEC.listOf().fieldOf("predicates").forGetter(SpeedrunGoal::predicates)
    ).apply(instance, SpeedrunGoal::new));

    public static final Codec<Holder> HOLDER_CODEC = Codec.lazyInitialized(() -> {
        // Stored as a resource location
        return Identifier.CODEC.comapFlatMap(
                id -> Optional.ofNullable(Holder.holders().get(id))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Can't find SpeedrunGoal.Holder " + id)),
                Holder::id
        );
    });

    public record Holder(Identifier id, SpeedrunGoal goal) implements Comparable<Holder> {
        private static volatile Map<Identifier, Holder> wrappedHolders = Collections.emptyMap();

        public static @Unmodifiable Map<Identifier, Holder> holders() {
            return Collections.unmodifiableMap(wrappedHolders);
        }

        public static void clearHolders() {
            wrappedHolders = Collections.emptyMap();
        }

        public static void setHolders(Map<Identifier, Holder> holders) {
            wrappedHolders = holders;
        }

        @Override
        public int compareTo(@NotNull SpeedrunGoal.Holder o) {
            // Key first
            int result = id.getNamespace().compareTo(o.id.getNamespace());
            if (result == 0) result = id.getPath().compareTo(o.id.getPath());
            return result;
        }

        public static Dialog toDialog() {
            List<DialogBody> dialogBodies = wrappedHolders.values().stream()
                    .sorted()
                    .<DialogBody>map(holder -> {
                        // should click to startup config dialog
                        return DialogUtil.itemBody(
                                holder.goal().icon(),
                                holder.goal().display().copy().withStyle(
                                        style -> style.withClickEvent(new ClickEvent.ShowDialog(
                                                net.minecraft.core.Holder.direct(holder.configDialog())
                                        ))
                                )
                        );
                    })
                    .toList();
            return new NoticeDialog(
                    DialogUtil.commonDialogData(
                            Component.translatable("gui.neospeedzero.goals"),
                            true,
                            dialogBodies
                    ),
                    NoticeDialog.DEFAULT_ACTION
            );
        }

        private Dialog configDialog() {
            List<SingleOptionInput.Entry> entries = SpeedrunDifficulties.entries().stream()
                    .map(entry -> {
                        // initial: default
                        return new SingleOptionInput.Entry(
                                entry.getKey().toString(),
                                Optional.of(entry.getValue().displayedNameHoverable()),
                                entry.getValue() == SpeedrunDifficulty.getDefault()
                        );
                    })
                    .toList();
            return new ConfirmationDialog(
                    DialogUtil.commonDialogData(
                            Component.translatable("gui.neospeedzero.startup_goal", goal().display()),
                            Collections.singletonList(new Input(
                                    "difficulty",
                                    DialogUtil.singleOptionInput(
                                            Component.translatable("gui.neospeedzero.select_difficulty"),
                                            entries
                                    )
                            ))
                    ),
                    new ActionButton(   // yes
                            DialogUtil.commonButtonData(CommonComponents.GUI_PROCEED),
                            Optional.of(DialogUtil.commandTemplate("/neospeed start " + id() + " $(difficulty)"))
                    ),
                    new ActionButton(   // no
                            DialogUtil.commonButtonData(CommonComponents.GUI_CANCEL),
                            Optional.of(new StaticAction(new ClickEvent.RunCommand("/neospeed list")))
                    )
            );
        }
    }
}
