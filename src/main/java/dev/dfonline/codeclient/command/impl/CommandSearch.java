package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class CommandSearch extends Command {
    @Override
    public String name() {
        return "ccsearch";
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        super.register(dispatcher, registryAccess);

        dispatcher.register(create(literal("search"), registryAccess));

    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.then(argument("query", greedyString()).suggests((context, builder) -> CommandJump.suggestJump(CommandJump.JumpType.ACTIONS, context, builder, false)).executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                var query = context.getArgument("query", String.class);
                var results = dev.scanForSigns(CommandJump.JumpType.ACTIONS.pattern, Pattern.compile("^.*" + Pattern.quote(query) + ".*$", Pattern.CASE_INSENSITIVE));

                if (results == null || results.isEmpty()) {
                    Utility.sendMessage(Component.translatable("codeclient.search.no_results"), ChatType.INFO);
                    return 0;
                }

                var message = Component.translatable("codeclient.search.results");
                results.forEach((pos, text) -> {
                    var type = text.getMessage(0, false).getString();
                    var name = text.getMessage(1, false).getString().trim();

                    var highlightAction = Component.empty().append(" [⏼]").setStyle(Style.EMPTY
                            .withColor(0xFF7FAA)
                            .withClickEvent(new ClickEvent.RunCommand(String.format("/highlight %s %s %s", pos.getX(), pos.getY(), pos.getZ())))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("codeclient.search.hover.highlight", pos.getX(), pos.getY(), pos.getZ())))
                    );

                    Style actionStyle = getActionColor(type);

                    var entry = Component.empty().append("\n ⏹ ").setStyle(actionStyle
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.empty().append(type).setStyle(actionStyle))
                                    )
                            )
                            .append(Component.empty().append(name).setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.RunCommand(String.format("/ptp %s %s %s", pos.getX(), pos.getY(), pos.getZ())))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("codeclient.search.hover.teleport", pos.getX(), pos.getY(), pos.getZ())))
                            ))
                            .append(highlightAction);
                    message.append(entry);
                });

                Utility.sendMessage(message, ChatType.SUCCESS);
            } else {
                Utility.sendMessage(Component.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
            }
            return 0;
        }));
    }

    private static Style getActionColor(String type) {
        return switch (type) {
            case "CONTROL" -> Style.EMPTY.withColor(ChatFormatting.BLACK);
            case "SELECT OBJECT" -> Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
            case "REPEAT" -> Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);
            case "SET VARIABLE" -> Style.EMPTY.withColor(ChatFormatting.WHITE);
            case "GAME ACTION" -> Style.EMPTY.withColor(ChatFormatting.RED);
            case "IF GAME" -> Style.EMPTY.withColor(ChatFormatting.DARK_RED);
            case "ENTITY ACTION" -> Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
            case "PLAYER ACTION" -> Style.EMPTY.withColor(ChatFormatting.GRAY);
            case "IF PLAYER" -> Style.EMPTY.withColor(ChatFormatting.GOLD);
            case "CALL FUNCTION" -> Style.EMPTY.withColor(ChatFormatting.BLUE);
            case "START PROCESS" -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case "IF ENTITY" -> Style.EMPTY.withColor(0xFFA85B);
            default -> Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
        };
    }
}
