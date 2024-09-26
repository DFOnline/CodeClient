package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandDelete extends TemplateActionCommand {
    @Override
    public String name() {
        return "delete";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.then(argument("path", greedyString()).suggests(this::suggestTemplates).executes(context -> {
            Path path = FileManager.templatesPath().resolve(context.getArgument("path", String.class));
            Path dft = path.getParent().resolve(path.getFileName() + ".dft");
            if (Files.notExists(path) && Files.exists(dft)) path = dft;
            if (!path.toAbsolutePath().startsWith(FileManager.templatesPath().toAbsolutePath())) {
                Utility.sendMessage(Text.translatable("codeclient.files.not_in_templates"), ChatType.INFO);
                return -2;
            }
            if (Files.notExists(path)) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.not_found"), ChatType.FAIL);
                return -1;
            }
            try {
                FileUtils.forceDelete(path.toFile());
                Utility.sendMessage(Text.translatable("codeclient.files.deleted", path), ChatType.SUCCESS);
            } catch (Exception e) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.cant_delete"), ChatType.FAIL);
            }
            return 0;
        }));
    }
}
