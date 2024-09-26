package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.impl.ScanPlot;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandScanPlot extends TemplateActionCommand {
    @Override
    public String name() {
        return "scanplot";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.then(argument("folder", greedyString()).suggests(this::suggestDirectories).executes(context -> {
            if (CodeClient.location instanceof Dev) {
                String arg = context.getArgument("folder", String.class);
                String[] path = arg.split("/");

                Path currentPath = FileManager.templatesPath();
                for (String dir : path) {
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

                boolean invalid = false;
                try {
                    var list = Files.list(currentPath);
                    for (var file : list.toList()) {
                        invalid = true;
                        if (file.getFileName().toString().equals(".git")) {
                            Utility.sendMessage(
                                    Text.empty()
                                            .append(Text.translatable("codeclient.files.git"))
                                            .append(Text.literal("\n"))
                                            .append(Text.translatable("codeclient.files.open_native").fillStyle(
                                                    Style.EMPTY
                                                            .withColor(Formatting.AQUA)
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, currentPath.toAbsolutePath().toString()))
                                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(currentPath.toAbsolutePath().toString())))))
                                    , ChatType.INFO);
                            invalid = false;
                            break;
                        }
                    }
                    list.close();
                } catch (Exception ignored) {
                    Utility.sendMessage(Text.translatable("codeclient.files.error.read_folder"), ChatType.FAIL);
                }
                if (invalid) {
                    Utility.sendMessage(Text.translatable("codeclient.files.empty_dir"));
                    return -1;
                }

                Utility.sendMessage(Text.translatable("codeclient.action.scanning").append(" ").append(Text.translatable("codeclient.action.abort")), ChatType.INFO);
                var scan = new ArrayList<ItemStack>();
                Path finalCurrentPath = currentPath;
                CodeClient.currentAction = new ScanPlot(() -> {
                    actionCallback();
                    for (ItemStack item : scan) {
                        String data = Utility.templateDataItem(item);
                        var template = Template.parse64(data);
                        if (template == null) continue;
                        var first = template.blocks.get(0);
                        String name = Objects.requireNonNullElse(first.action != null ? first.action : first.data, "unknown").replaceAll(SystemUtils.IS_OS_WINDOWS ? "[<>:\"/|?*]" : "/", "");
                        var filePath = finalCurrentPath.resolve(name + ".dft");
                        try {
                            Files.write(filePath, Base64.getDecoder().decode(data));
                        } catch (Exception ignored) {
                            Utility.sendMessage(Text.translatable("codeclient.files.error.write_file", filePath.toString()), ChatType.FAIL);
                        }
                    }

                }, scan);
                CodeClient.currentAction.init();
                return 0;
            } else {
                Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                return 1;
            }
        }));
    }


}
