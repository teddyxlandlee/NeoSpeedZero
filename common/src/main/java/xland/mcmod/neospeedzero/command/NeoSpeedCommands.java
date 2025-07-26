package xland.mcmod.neospeedzero.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.api.SpeedrunStartupConfig;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;

import java.util.function.Consumer;

import static net.minecraft.commands.Commands.*;

public class NeoSpeedCommands {
    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            // Register commands here...
            dispatcher.register(literal("neospeed")
                    .then(literal("start")
                            .then(argument("goal", ResourceLocationArgument.id())
                                    .suggests((context, builder) ->
                                            SharedSuggestionProvider.suggestResource(SpeedrunGoal.Holder.holders().keySet(), builder))
                                    .executes(context -> {
                                        ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");
                                        ServerPlayer player = context.getSource().getPlayerOrException();

                                        SpeedrunStartupConfig startupConfig = SpeedrunStartupConfig.builder()
                                                .goal(goalId)
                                                .build();
                                        NeoSpeedLifecycle.startSpeedrun(player, startupConfig).ifPresent(sendFailure(context));
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(argument("difficulty", ResourceLocationArgument.id())
                                            .suggests((context, builder) ->
                                                    SharedSuggestionProvider.suggestResource(SpeedrunDifficulties.keys(), builder))
                                            .executes(context -> {
                                                ResourceLocation goalId = ResourceLocationArgument.getId(context, "goal");
                                                ResourceLocation difficultyId = ResourceLocationArgument.getId(context, "difficulty");
                                                ServerPlayer player = context.getSource().getPlayerOrException();

                                                SpeedrunStartupConfig startupConfig = SpeedrunStartupConfig.builder()
                                                        .goal(goalId)
                                                        .difficulty(difficultyId)
                                                        .build();
                                                NeoSpeedLifecycle.startSpeedrun(player, startupConfig).ifPresent(sendFailure(context));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
                    .then(literal("stop")
                            .executes(context -> {
                                NeoSpeedLifecycle.stopSpeedrun(context.getSource().getPlayerOrException()).ifPresent(sendFailure(context));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(literal("view")
                            .executes(context -> {
                                final ServerPlayer player = context.getSource().getPlayerOrException();
                                final SpeedrunRecord record = player.ns0$currentRecord();
                                if (record == null) {
                                    context.getSource().sendFailure(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
                                    return 0;
                                }
                                NeoSpeedLifecycle.viewRecord(player, record);
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        });
    }

    private static Consumer<Component> sendFailure(CommandContext<CommandSourceStack> context) {
        return component -> context.getSource().sendFailure(component);
    }
}
