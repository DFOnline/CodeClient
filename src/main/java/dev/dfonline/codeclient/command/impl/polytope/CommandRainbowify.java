package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandRainbowify extends Command {
    @Override
    public String name() {
        return "rainbowify";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
                .executes((context) -> {
                    String sourceString = context.getArgument("text", String.class);

                                        /*boolean bold = context.getArgument("bold", Boolean.class);
                                        boolean italic = context.getArgument("italic", Boolean.class);
                                        boolean strikethrough = context.getArgument("strikethrough", Boolean.class);
                                        boolean underline = context.getArgument("underline", Boolean.class);
                                        boolean obfuscate = context.getArgument("obfuscate", Boolean.class);*/

                    //MutableText base = Text.empty();

                    StringBuilder rainbowBuf = new StringBuilder(sourceString.length());

                    for (int i = 0; i < sourceString.length(); i++) {
                        char srcChar = sourceString.charAt(i);
                        if (srcChar != ' ') {
                            rainbowBuf.append(TextReplacer.rainbowColors[i % 7]);
                            // base.append(
                                                        /*Text.literal(Character.toString(srcChar))
                                                                .setStyle(Style.EMPTY
                                                                        .withColor(rainbowColors[i % 7])
                                                                        *//*.withBold(bold)
                                                                        .withItalic(italic)
                                                                        .withStrikethrough(strikethrough)
                                                                        .withUnderline(underline)
                                                                        .withObfuscated(obfuscate)*//*
                                                                )
                                                        );*/
                        }
                        rainbowBuf.append(srcChar);
                    }


                    Text msg =
                            Text.literal("Click here to copy '%s'".formatted(rainbowBuf.toString()))
                                    .setStyle(
                                            Style.EMPTY
                                                    .withColor(Formatting.GREEN)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, rainbowBuf.toString()))
                                    );

                    MC.player.sendMessage(msg, false);

                    return 1;
                }));
    }
}
