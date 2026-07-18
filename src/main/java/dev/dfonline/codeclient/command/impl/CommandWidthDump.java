package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.Font;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;

public class CommandWidthDump extends Command {
    @Override
    public String name() {
        return "widthdump";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            StringBuilder data = new StringBuilder("CODECLIENT WIDTHDUMP\nFORMAT GOES AS\n<UNICODE> <WIDTH>\n");
            for (int codePoint = Character.MIN_CODE_POINT; codePoint <= Character.MAX_CODE_POINT; codePoint++) {
                String character = new String(Character.toChars(codePoint));
                Font renderer = CodeClient.MC.font;
                data.append(character).append(" ").append(renderer.width(character)).append("\n");
            }
            String dataFinal = data.toString();
            try {
                Path path = FileManager.writeFile("widthdump.txt", dataFinal);
                Utility.sendMessage(Component.translatable("codeclient.files.saved", path).setStyle(Component.empty().getStyle().withClickEvent(new ClickEvent.OpenFile(path.toString()))));
            } catch (Exception ignored) {
                Utility.sendMessage(Component.translatable("codeclient.files.error.cant_save"), ChatType.FAIL);
                CodeClient.LOGGER.info(dataFinal);
            }
            return 0;
        });
    }
}
