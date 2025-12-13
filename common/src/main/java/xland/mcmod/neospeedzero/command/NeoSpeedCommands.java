package xland.mcmod.neospeedzero.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.NeoSpeedLifecycle;
import xland.mcmod.neospeedzero.api.SpeedrunDifficulties;
import xland.mcmod.neospeedzero.api.SpeedrunStartupConfig;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.resource.SpeedrunGoal;
import xland.mcmod.neospeedzero.util.event.PlatformEvents;

import java.util.function.Consumer;

import static net.minecraft.commands.Commands.*;

public final class NeoSpeedCommands {
    public static void register() {
        //CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
        PlatformEvents.getInstance().registerCommand(dispatcher -> {
            // Register commands here...
            dispatcher.register(literal("neospeed")
                    .then(literal("start")
                            .then(argument("goal", IdentifierArgument.id())
                                    .suggests((context, builder) ->
                                            SharedSuggestionProvider.suggestResource(SpeedrunGoal.Holder.holders().keySet(), builder))
                                    .executes(context -> {
                                        Identifier goalId = IdentifierArgument.getId(context, "goal");
                                        ServerPlayer player = context.getSource().getPlayerOrException();

                                        SpeedrunStartupConfig startupConfig = SpeedrunStartupConfig.builder()
                                                .goal(goalId)
                                                .build();
                                        NeoSpeedLifecycle.startSpeedrun(player, startupConfig).ifPresent(sendFailure(context));
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(argument("difficulty", IdentifierArgument.id())
                                            .suggests((context, builder) ->
                                                    SharedSuggestionProvider.suggestResource(SpeedrunDifficulties.keys(), builder))
                                            .executes(context -> {
                                                Identifier goalId = IdentifierArgument.getId(context, "goal");
                                                Identifier difficultyId = IdentifierArgument.getId(context, "difficulty");
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
                            .then(literal("raw")
                                    .executes(context -> {
                                        final ServerPlayer player = context.getSource().getPlayerOrException();
                                        final SpeedrunRecord record = player.ns0$currentRecord();
                                        if (record == null) {
                                            context.getSource().sendFailure(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
                                            return 0;
                                        }
                                        NeoSpeedLifecycle.viewRecordRaw(player, record);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(literal("dialog")
                                    .executes(context -> {
                                        final ServerPlayer player = context.getSource().getPlayerOrException();
                                        final SpeedrunRecord record = player.ns0$currentRecord();
                                        if (record == null) {
                                            context.getSource().sendFailure(Component.translatable("message.neospeedzero.record.stop.absent", player.getDisplayName()));
                                            return 0;
                                        }
                                        NeoSpeedLifecycle.viewRecordDialog(player, record);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(literal("list")
                            .executes(context -> {
                                final ServerPlayer player = context.getSource().getPlayerOrException();
                                NeoSpeedLifecycle.listDialog(player);
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(literal("join")
                            .then(literal("player")
                                    .then(argument("maybeHost", EntityArgument.player())
                                            .executes(context -> {
                                                final ServerPlayer me = context.getSource().getPlayerOrException();
                                                final ServerPlayer maybeHost = EntityArgument.getPlayer(context, "maybeHost");

                                                NeoSpeedLifecycle.joinSpeedrun(me, RecordReference.ofPlayer(maybeHost)).ifPresent(sendFailure(context));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(literal("record")
                                    .then(argument("reference", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                context.getSource().getServer().ns0$recordManager().getAllRecordIds().forEach(uuid -> {
                                                    // Due to performance concerns, detailed tooltips won't be suggested so far
                                                    builder.suggest(uuid.toString());
                                                });
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {
                                                final ServerPlayer me = context.getSource().getPlayerOrException();
                                                var ref = RecordReference.of(StringArgumentType.getString(context, "reference"));

                                                NeoSpeedLifecycle.joinSpeedrun(me, ref).ifPresent(sendFailure(context));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
                    .then(literal("quit")
                            .executes(context -> {
                                NeoSpeedLifecycle.quitSpeedrun(context.getSource().getPlayerOrException()).ifPresent(sendFailure(context));
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        });
    }

    private static Consumer<Component> sendFailure(CommandContext<CommandSourceStack> context) {
        return component -> context.getSource().sendFailure(component);
    }

    private NeoSpeedCommands() {}
}
