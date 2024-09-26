package dev.dfonline.codeclient.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dfonline.codeclient.FileManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class TemplateActionCommand extends ActionCommand {
    protected CompletableFuture<Suggestions> suggestDirectories(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        return suggestTemplates(context, builder, false);
    }

    protected CompletableFuture<Suggestions> suggestTemplates(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        return suggestTemplates(context, builder, true);
    }

    private static CompletableFuture<Suggestions> suggestTemplates(CommandContext<FabricClientCommandSource> ignored, SuggestionsBuilder builder, boolean suggestFiles) {
        try {
            var possibilities = new ArrayList<String>();

            String[] path = builder.getRemaining().split("/", -1);
            String currentPath = String.join("/", (Arrays.stream(path).toList().subList(0, path.length - 1)));
            var list = Files.list(FileManager.templatesPath().resolve(currentPath));
            for (var file : list.toList()) {
                if (file.getFileName().toString().equals(".git")) continue;
                String s = currentPath.isEmpty() ? "" : currentPath + "/";
                if (Files.isDirectory(file)) possibilities.add(s + file.getFileName().toString() + "/");
                else if (file.getFileName().toString().endsWith(".dft")) {
                    String name = file.getFileName().toString();
                    if (suggestFiles) possibilities.add(s + name.substring(0, name.length() - 4));
                }
            }
            list.close();
            for (String possibility : possibilities) {
                if (possibility.toLowerCase().contains(builder.getRemainingLowerCase()))
                    builder.suggest(possibility, Text.literal("Folder"));
            }
        } catch (IOException ignored1) {
        }
        return CompletableFuture.completedFuture(builder.build());
    }

    protected static List<String> getAllTemplates(Path path) throws IOException {
        if (Files.notExists(path)) {
            Path asName = path.getParent().resolve(path.getFileName() + ".dft");
            if (Files.exists(asName)) return getAllTemplates(asName);
            return null;
        }
        if (Files.isDirectory(path)) {
            var list = new ArrayList<String>();
            var files = Files.list(path);
            for (var file : files.toList()) list.addAll(getAllTemplates(file));
            files.close();
            return list;
        } else {
            byte[] data = Files.readAllBytes(path);
            return Collections.singletonList(new String(Base64.getEncoder().encode(data)));
        }
    }
}
