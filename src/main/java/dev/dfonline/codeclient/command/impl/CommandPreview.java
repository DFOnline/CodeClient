package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.NotNull;

import static dev.dfonline.codeclient.Utility.componentToText;
import static dev.dfonline.codeclient.hypercube.HypercubeMiniMessage.MM;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class CommandPreview extends Command {
    @Override
    public String name() {
        return "preview";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> 0)
                .then(
                        literal("actionbar").then(argument("text", StringArgumentType.greedyString()).executes(context -> {
                            if (CodeClient.MC.player == null) return 0;
                            CodeClient.MC.gui.hud.setOverlayMessage(componentToText(getText(context)), false);
                            return 0;
                        }))
                ).then(
                        literal("title").then(argument("text", StringArgumentType.greedyString()).executes(context -> {
                            if (CodeClient.MC.player == null) return 0;
                            CodeClient.MC.gui.hud.setTitle(componentToText(getText(context)));
                            return 0;
                        }))
                ).then(
                        literal("subtitle").then(argument("text", StringArgumentType.greedyString()).executes(context -> {
                            if (CodeClient.MC.player == null) return 0;
                            CodeClient.MC.gui.hud.setSubtitle(componentToText(getText(context)));
                            return 0;
                        }))
                );
    }

    private static @NotNull Component getText(CommandContext<FabricClientCommandSource> context) {
        return MM.deserialize(context.getArgument("text", String.class));
    }
}
