package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandSmcaps extends Command {
    @Override
    public String name() {
        return "smcaps";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                .executes((context -> {

                    // lower case since latin small caps only has one case
                    String sourceString = context.getArgument("text", String.class).toLowerCase(Locale.ROOT);

                    String smCapString = TextReplacer.replaceFromMapping(sourceString, TextReplacer.smallCaps);

                    Text msg =
                            Text.literal("Click here to copy '%s'".formatted(smCapString))
                                    .setStyle(
                                            Style.EMPTY
                                                    .withColor(Formatting.GREEN)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, smCapString))
                                    );

                    MC.player.sendMessage(msg, false);

                    return 1;
                }))
        );
    }
}
