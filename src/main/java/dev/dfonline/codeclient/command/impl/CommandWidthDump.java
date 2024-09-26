package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class CommandWidthDump extends Command {
    @Override
    public String name() {
        return "widthdump";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            StringBuilder data = new StringBuilder("CODECLIENT WIDTHDUMP\nFORMAT GOES AS\n<UNICODE> <WIDTH>\n");
            for (int codePoint = Character.MIN_CODE_POINT; codePoint <= Character.MAX_CODE_POINT; codePoint++) {
                String character = new String(Character.toChars(codePoint));
                TextRenderer renderer = CodeClient.MC.textRenderer;
                data.append(character).append(" ").append(renderer.getWidth(character)).append("\n");
            }
            String dataFinal = data.toString();
            try {
                Path path = FileManager.writeFile("widthdump.txt", dataFinal);
                Utility.sendMessage(Text.translatable("codeclient.files.saved", path).setStyle(Text.empty().getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toString()))));
            } catch (Exception ignored) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.cant_save"), ChatType.FAIL);
                CodeClient.LOGGER.info(dataFinal);
            }
            return 0;
        });
    }
}
