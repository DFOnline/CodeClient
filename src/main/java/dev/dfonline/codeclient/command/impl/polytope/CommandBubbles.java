package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.dev.debug.Debug;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandBubbles extends Command {
    @Override
    public String name() {
        return "bubbles";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                .executes((context -> {
                    String sourceString = context.getArgument("text", String.class);

                    String bubbleString = TextReplacer.replaceFromMapping(sourceString, TextReplacer.bubbles);

                    Text msg =
                            Text.literal("Click here to copy '%s'".formatted(bubbleString))
                                    .setStyle(
                                            Style.EMPTY
                                                    .withColor(Formatting.GREEN)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, bubbleString))
                                    );

                    CodeClient.MC.player.sendMessage(msg, false);

                    return 1;
                }))
        );
    }
}
