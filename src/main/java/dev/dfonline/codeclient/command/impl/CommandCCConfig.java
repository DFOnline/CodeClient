package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.config.Config;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class CommandCCConfig extends Command {
    @Override
    public String name() {
        return "ccconfig";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            CodeClient.screenToOpen = Config.getConfig().getLibConfig().generateScreen(null);
            return 0;
        }).then(argument("option", string()).suggests((context, builder) -> {
            var option = builder.getRemaining();
            for (var field : Config.class.getFields()) {
                if (field.getName().toLowerCase().startsWith(option.toLowerCase())) builder.suggest(field.getName());
            }
            return builder.buildFuture();
        }).executes(context -> {
            var option = context.getArgument("option", String.class);
            try {
                Utility.sendMessage(
                        Component.translatable("codeclient.config.command.query",
                                Component.literal(option).withStyle(ChatFormatting.AQUA),
                                Component.literal(String.valueOf(Config.class.getField(option).get(Config.getConfig()))).withStyle(ChatFormatting.AQUA)));
            } catch (Exception e) {
                Utility.sendMessage(Component.translatable("codeclient.config.command.query.fail", Component.literal(option).withStyle(ChatFormatting.YELLOW)), ChatType.FAIL);
            }
            return 0;
        }).then(argument("value", greedyString()).suggests((context, builder) -> {
            var option = context.getArgument("option", String.class);
            try {
                var value = builder.getRemaining();
                var field = Config.class.getField(option);
                List<String> options;
                if (field.getType().equals(boolean.class)) {
                    options = List.of("true", "false");
                } else if (field.getType().isEnum()) {
                    options = new ArrayList<>();
                    for (Object member : field.getType().getEnumConstants()) {
                        options.add(((Enum<?>) member).name());
                    }
                } else options = List.of();
                for (var possibility : options) {
                    if (possibility.toLowerCase().startsWith(value.toLowerCase())) builder.suggest(possibility);
                }
            } catch (Exception ignored) {
            }
            return builder.buildFuture();
        }).executes(context -> {
            var option = context.getArgument("option", String.class);
            try {
                var value = context.getArgument("value", String.class);
                var field = Config.class.getField(option);
                if (field.getType().equals(boolean.class)) {
                    var bool = Boolean.valueOf(value);
                    field.set(Config.getConfig(), bool);
                    Utility.sendMessage(Component.translatable(bool ? "codeclient.config.command.enable" : "codeclient.config.command.disable", Component.literal(option).withStyle(ChatFormatting.AQUA)), ChatType.SUCCESS);
                    Config.getConfig().save();
                    return 0;
                } else if (field.getType().isEnum()) {
                    for (Object member : field.getType().getEnumConstants()) {
                        if (((Enum<?>) member).name().equalsIgnoreCase(value)) {
                            field.set(Config.getConfig(), member);
                            Utility.sendMessage(Component.translatable("codeclient.config.command.set", Component.literal(option).withStyle(ChatFormatting.AQUA), Component.literal(value).withStyle(ChatFormatting.AQUA)), ChatType.SUCCESS);
                            Config.getConfig().save();
                            return 0;
                        }
                    }
                    Utility.sendMessage(Component.translatable("codeclient.config.command.set.fail", Component.literal(option).withStyle(ChatFormatting.YELLOW), Component.literal(value).withStyle(ChatFormatting.YELLOW)), ChatType.FAIL);
                    return -1;
                }
                Utility.sendMessage(Component.translatable("codeclient.config.command.fail"), ChatType.FAIL);
            } catch (Exception ignored) {
            }
            return 0;
        })));
    }
}
