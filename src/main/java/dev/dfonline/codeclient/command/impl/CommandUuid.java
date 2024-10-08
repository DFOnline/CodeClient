package dev.dfonline.codeclient.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.command.argument.PlayerArgumentType;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandUuid extends Command {
    @Override
    public String name() {
        return "uuid";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("username", PlayerArgumentType.player())
                .executes(context -> {
                    String name = context.getArgument("username", String.class);
                    return sendUuid(name);
                }))
                .executes(context -> {
                    String name = CodeClient.MC.getGameProfile().getName();
                    return sendUuid(name);
                });
    }

    private int sendUuid(String username) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        MinecraftClient mc = CodeClient.MC;
        if(mc.getNetworkHandler() == null) return -1;

        try {
            String UUIDJson = IOUtils
                    .toString(new URL(url), StandardCharsets.UTF_8);
            if (UUIDJson.isEmpty()) {
                Utility.sendMessage(Text.translatable("codeclient.command.uuid.not_found", username), ChatType.FAIL);
                return -1;
            }
            JsonObject json = JsonParser.parseString(UUIDJson).getAsJsonObject();
            String uuid = json.get("id").getAsString();
            String fullUUID = Utility.fromTrimmed(uuid);

            Text uuidDisplay = Text.literal(fullUUID).formatted(Formatting.AQUA, Formatting.UNDERLINE);

            Text message = Text.translatable("codeclient.command.uuid", username, uuidDisplay)
                    .fillStyle(Style.EMPTY
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.hover.click_to_copy")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fullUUID)));

            Utility.sendMessage(message, ChatType.SUCCESS);

            if (CodeClient.location instanceof Dev && Config.getConfig().GiveUuidNameStrings)
                mc.getNetworkHandler().sendCommand("str " + fullUUID);

        } catch (IOException e) {
            Utility.sendMessage(Text.translatable("codeclient.command.uuid.not_found", username), ChatType.FAIL);
        }

        return 0;
    }
}
