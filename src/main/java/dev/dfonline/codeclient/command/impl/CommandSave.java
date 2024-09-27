package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandSave extends TemplateActionCommand {
    @Override
    public String name() {
        return "save";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("path", greedyString()).suggests(this::suggestDirectories).executes(context -> {
            if (CodeClient.MC.player == null) return -1;
            String data = Utility.templateDataItem(CodeClient.MC.player.getMainHandStack());
            if (data == null) {
                Utility.sendMessage(Text.translatable("codeclient.files.hold_template"), ChatType.FAIL);
                return 0;
            }

            String arg = context.getArgument("path", String.class);
            String[] path = arg.split("/");
            Path currentPath = FileManager.templatesPath();
            // Create folders
            for (String dir : Arrays.stream(path).toList().subList(0, path.length - 1)) {
                currentPath = currentPath.resolve(dir);
                if (Files.notExists(currentPath)) {
                    try {
                        Files.createDirectory(currentPath);
                    } catch (Exception ignored) {
                        Utility.sendMessage(Text.translatable("codeclient.files.error.write_folder", currentPath));
                    }
                } else if (!Files.isDirectory(currentPath)) {
                    Utility.sendMessage(Text.translatable("codeclient.files.error.not_dir", currentPath), ChatType.FAIL);
                    return -1;
                }
            }
            currentPath = currentPath.resolve(path[path.length - 1] + ".dft");
            // Write file
            try {
                Files.write(currentPath.resolve(currentPath), Base64.getDecoder().decode(data));
                Utility.sendMessage(Text.translatable("codeclient.files.saved", currentPath), ChatType.SUCCESS);
            } catch (Exception e) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.write_file", currentPath), ChatType.FAIL);
                CodeClient.LOGGER.error(e.getMessage());
            }
            return 0;
        }));
    }
}
