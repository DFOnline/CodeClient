package dev.dfonline.codeclient.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.config.Config;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandName extends Command {
    @Override
    public String name() {
        return "name";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("uuid", word())
                        .executes(context -> {
                            String uuid = context.getArgument("uuid", String.class);
                            MinecraftClient mc = CodeClient.MC;
                            if(mc.getNetworkHandler() == null) return -1;

                            String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;

                            try {
                                String NameJson = IOUtils
                                        .toString(new URL(url), StandardCharsets.UTF_8);
                                if (NameJson.isEmpty()) {
                                    Utility.sendMessage(Text.translatable("codeclient.command.name.not_found", uuid), ChatType.FAIL);
                                    return -1;
                                }

                                JsonObject json = JsonParser.parseString(NameJson).getAsJsonObject();
                                String fullName = json.get("name").getAsString();

                                String localUsername = Objects.requireNonNull(mc.player).getGameProfile().getName();

                                if(localUsername.equals(fullName)) {
                                    Utility.sendMessage(Text.translatable("codeclient.command.name.own"), ChatType.SUCCESS);
                                    return 0;
                                } else {
                                    Text message = Text.empty()
                                            .append(Text.translatable("codeclient.command.name", uuid))
                                            .append(Text.literal(fullName).formatted(Formatting.AQUA, Formatting.UNDERLINE))
                                            .append(Text.literal("!").formatted(Formatting.RESET))
                                            .fillStyle(Style.EMPTY
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy to clipboard")))
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fullName)));

                                    Utility.sendMessage(message, ChatType.SUCCESS);

                                    if (CodeClient.location.name().equals("code") && Config.getConfig().GiveUuidNameStrings)
                                        mc.getNetworkHandler().sendCommand("str " + fullName);
                                    
                                    return 0;
                                }

                            } catch (IOException e) {
                                Utility.sendMessage(Text.translatable("codeclient.command.name.not_found", uuid), ChatType.FAIL);
                                return -1;
                            }
                        }));
    }
}
