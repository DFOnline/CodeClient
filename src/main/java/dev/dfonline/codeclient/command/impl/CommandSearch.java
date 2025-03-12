package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandSearch extends Command {
    @Override
    public String name() {
        return "ccsearch";
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        super.register(dispatcher, registryAccess);

        dispatcher.register(create(literal("search"), registryAccess));

    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("query", greedyString()).suggests((context, builder) -> CommandJump.suggestJump(CommandJump.JumpType.ACTIONS, context, builder, false)).executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                var query = context.getArgument("query", String.class);
                var results = dev.scanForSigns(CommandJump.JumpType.ACTIONS.pattern, Pattern.compile("^.*" + Pattern.quote(query) + ".*$", Pattern.CASE_INSENSITIVE));

                if (results == null || results.isEmpty()) {
                    Utility.sendMessage(Text.translatable("codeclient.search.no_results"), ChatType.INFO);
                    return 0;
                }

                var message = Text.translatable("codeclient.search.results");
                results.forEach((pos, text) -> {
                    var type = text.getMessage(0, false).getString();
                    var name = text.getMessage(1, false).getString().trim();

                    var highlightAction = Text.empty().append(" [⏼]").setStyle(Style.EMPTY
                            .withColor(0xFF7FAA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/highlight %s %s %s", pos.getX(), pos.getY(), pos.getZ())))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.search.hover.highlight", pos.getX(), pos.getY(), pos.getZ())))
                    );

                    Style actionStyle = getActionColor(type);

                    var entry = Text.empty().append("\n ⏹ ").setStyle(actionStyle
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.empty().append(type).setStyle(actionStyle))
                                    )
                            )
                            .append(Text.empty().append(name).setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ptp %s %s %s", pos.getX(), pos.getY(), pos.getZ())))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.search.hover.teleport", pos.getX(), pos.getY(), pos.getZ())))
                            ))
                            .append(highlightAction);
                    message.append(entry);
                });

                Utility.sendMessage(message, ChatType.SUCCESS);
            } else {
                Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
            }
            return 0;
        }));
    }

    private static Style getActionColor(String type) {
        return switch (type) {
            case "CONTROL" -> Style.EMPTY.withColor(Formatting.BLACK);
            case "SELECT OBJECT" -> Style.EMPTY.withColor(Formatting.LIGHT_PURPLE);
            case "REPEAT" -> Style.EMPTY.withColor(Formatting.DARK_AQUA);
            case "SET VARIABLE" -> Style.EMPTY.withColor(Formatting.WHITE);
            case "GAME ACTION" -> Style.EMPTY.withColor(Formatting.RED);
            case "IF GAME" -> Style.EMPTY.withColor(Formatting.DARK_RED);
            case "ENTITY ACTION" -> Style.EMPTY.withColor(Formatting.DARK_GREEN);
            case "PLAYER ACTION" -> Style.EMPTY.withColor(Formatting.GRAY);
            case "IF PLAYER" -> Style.EMPTY.withColor(Formatting.GOLD);
            case "CALL FUNCTION" -> Style.EMPTY.withColor(Formatting.BLUE);
            case "START PROCESS" -> Style.EMPTY.withColor(Formatting.GREEN);
            case "IF ENTITY" -> Style.EMPTY.withColor(0xFFA85B);
            default -> Style.EMPTY.withColor(Formatting.DARK_GRAY);
        };
    }
}
