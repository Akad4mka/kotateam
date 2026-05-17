package net.arm.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KotateamCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("kotateam")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");

                                    if (!ArmTeamMateClient.config.teammates.contains(name)) {
                                        ArmTeamMateClient.config.teammates.add(name);
                                        ArmTeamMateClient.config.save();

                                        ctx.getSource().sendFeedback(Text.literal("Игрок " + name + " добавлен в тиммейты").formatted(Formatting.GREEN));
                                    } else {
                                        ctx.getSource().sendError(Text.literal("Игрок уже в списке"));
                                    }
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");

                                    if (ArmTeamMateClient.config.teammates.contains(name)) {
                                        ArmTeamMateClient.config.teammates.remove(name);
                                        ArmTeamMateClient.config.save();

                                        ctx.getSource().sendFeedback(Text.literal("Игрок " + name + " удален").formatted(Formatting.RED));
                                    } else {
                                        ctx.getSource().sendError(Text.literal("Игрок не найден в списке"));
                                    }
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            String list = String.join(", ", ArmTeamMateClient.config.teammates);
                            if (list.isEmpty()) list = "пусто";

                            ctx.getSource().sendFeedback(Text.literal("Список тиммейтов: " + list).formatted(Formatting.YELLOW));
                            return 1;
                        }))
        );
    }
}