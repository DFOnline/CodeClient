package dev.dfonline.codeclient;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.*;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {
    private static final Text DONE = Text.translatable("gui.done");
    public static Action confirm = null;

    private static void actionCallback() {
        CodeClient.currentAction = new None();
        Utility.sendMessage(DONE, ChatType.SUCCESS);
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("auth").executes(context -> {
            SocketHandler.setAuthorised(true);
            Utility.sendMessage(Text.translatable("codeclient.api.authorised")
                            .append(Text.literal("\n")).append(Text.translatable("codeclient.api.warning").formatted(Formatting.BOLD))
                            .append(Text.literal("\n")).append(Text.translatable("codeclient.api.remove"))
                    , ChatType.SUCCESS);
            return 0;
        }).then(literal("remove").executes(context -> {
            SocketHandler.setAuthorised(false);
            Utility.sendMessage(Text.translatable("codeclient.api.removed"), ChatType.SUCCESS);
            return 0;
        })).then(literal("disconnect").executes(context -> {
            SocketHandler.setConnection(null);
            return 0;
        })));


        dispatcher.register(literal("worldplot").executes(context -> {
            if (CodeClient.location instanceof Plot plot) plot.setSize(null);
            return 0;
        }).then(literal("basic").executes(context -> {
            if (CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.BASIC);
            return 0;
        })).then(literal("large").executes(context -> {
            if (CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.LARGE);
            return 0;
        })).then(literal("massive").executes(context -> {
            if (CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.MASSIVE);
            return 0;
        })));


        dispatcher.register(literal("fixcc").executes(context -> {
            CodeClient.reset();
            return 0;
        }));


        dispatcher.register(literal("abort").executes(context -> {
            confirm = null;
            CodeClient.currentAction = new None();
            return 0;
        }));


        dispatcher.register(literal("getactiondump").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.NONE, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        }).then(literal("section").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.SECTION, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        })).then(literal("ampersand").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.AMPERSAND, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        })));

        dispatcher.register(literal("widthdump").executes(context -> {
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
        }));

//        dispatcher.register(literal("back").executes(context -> {
//            if(CodeClient.location instanceof Creator plot) {
//                if(plot.devPos == null) {
//                    Utility.sendMessage("There is no position to go back to!", ChatType.FAIL);
//                    return 1;
//                }
//                if(!(CodeClient.currentAction instanceof None)) {
//                    Utility.sendMessage("CodeClient is currently busy, try again in a moment.",ChatType.FAIL);
//                    return 1;
//                }
//                if(LastPos.tpBack()) return 0;
//                else {
//                    Utility.sendMessage("An error occurred whilst trying to go back.",ChatType.FAIL);
//                    return 1;
//                }
//            }
//            else {
//                Utility.sendMessage("You must be in dev or build mode to do this!",ChatType.FAIL);
//                return 1;
//            }
//        }));

        dispatcher.register(literal("getspawn").executes(context -> {
            if (!(CodeClient.location instanceof Dev)) return 1;
            CodeClient.currentAction = new MoveToSpawn(Commands::actionCallback);
            CodeClient.currentAction.init();
            return 0;
        }));
        dispatcher.register(literal("getsize").executes(context -> {
            if (!(CodeClient.location instanceof Dev)) return 1;
            CodeClient.currentAction = new GetPlotSize(Commands::actionCallback);
            CodeClient.currentAction.init();
            return 0;
        }));
//        dispatcher.register(literal("clearplot").executes(context -> {
//            CodeClient.currentAction = new ClearPlot(() -> Utility.sendMessage("Done!", ChatType.SUCCESS));
//            CodeClient.currentAction.init();
//            return 0;
//        }));
//
//        dispatcher.register(literal("codeforme").executes(context -> {
//            if(!(CodeClient.location instanceof Dev)) return 1;
//            CodeClient.currentAction = new ClearPlot(() -> {
//                CodeClient.currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> {
//                    CodeClient.currentAction = new None();
//                    Utility.sendMessage("Done!", ChatType.SUCCESS);
//                });
//                CodeClient.currentAction.init();
//            });
//            CodeClient.currentAction.init();
//            return 0;
//        }));

        dispatcher.register(literal("scanplot")//.executes(context -> {
//            if(CodeClient.location instanceof Dev) {
//                var scan = new ArrayList<Template>();
//                CodeClient.currentAction = new ScanPlot(() -> {
//                    CodeClient.currentAction = new None();
//                    Utility.sendMessage("Done!", ChatType.SUCCESS);
//                    Utility.sendMessage("Results:", ChatType.INFO);
//                    for (Template template : scan) {
//                        Utility.sendMessage(String.valueOf(template.blocks.size()));
//                    }
//
//                },scan);
//                CodeClient.currentAction.init();
//                return 0;
//            }
//            else {
//                Utility.sendMessage("You must be in dev mode!", ChatType.FAIL);
//                return 1;
//            }
//        })
                        .then(argument("folder", StringArgumentType.greedyString()).suggests(Commands::suggestDirectories).executes(context -> {
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
                                        String name = Objects.requireNonNullElse(first.action != null ? first.action : first.data, "unknown");
                                        var filePath = finalCurrentPath.resolve(name + ".dft");
                                        try {
                                            Files.write(filePath, Base64.getDecoder().decode(data));
                                        } catch (Exception ignored) {
                                            Utility.sendMessage(Text.translatable("codeclient.files.error.write_file", filePath), ChatType.FAIL);
                                        }
                                    }

                                }, scan);
                                CodeClient.currentAction.init();
                                return 0;
                            } else {
                                Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                                return 1;
                            }
                        }))
        );
        dispatcher.register(literal("scanfor").then(argument("name", StringArgumentType.greedyString()).executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                Pattern pattern = Pattern.compile(context.getArgument("name", String.class), Pattern.CASE_INSENSITIVE);
                var scan = dev.scanForSigns(pattern);
                Utility.sendMessage(Text.translatable("codeclient.action.scanfor.scan_result"));
                for (var res : scan.entrySet()) {
                    Utility.sendMessage("- " + res.getKey() + ": " + res.getValue().getMessage(1, false).getString());
                }
                return 0;
            }
            Utility.sendMessage(Text.translatable("codeclient.action.scanfor.scan_fail"), ChatType.FAIL);
            return -1;
        })));
//        dispatcher.register(literal("swapininv").executes(context -> {
//            if(CodeClient.location instanceof Dev) {
//                PlaceTemplates action = Utility.createSwapper(Utility.templatesInInventory(), () -> {
//                    CodeClient.currentAction = new None();
//                    Utility.sendMessage("Done!", ChatType.SUCCESS);
//                });
//                if(action == null) return -2;
//                CodeClient.currentAction = action.swap();
//                CodeClient.currentAction.init();
//                return 0;
//            }
//            return -1;
//        }));
        dispatcher.register(literal("templateplacer").executes(context -> {
            if (CodeClient.location instanceof Dev) {
                var action = Utility.createPlacer(Utility.templatesInInventory(), Commands::actionCallback);
                if (action == null) return -1;
                CodeClient.currentAction = action;
                CodeClient.currentAction.init();
                return 0;
            }
            Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
            return -1;
        }).then(argument("path", StringArgumentType.greedyString()).suggests(Commands::suggestTemplates).executes(context -> {
            try {
                if (CodeClient.location instanceof Dev dev) {
                    var map = new HashMap<BlockPos, ItemStack>();
                    BlockPos pos = dev.findFreePlacePos();
                    for (var template : Objects.requireNonNull(getAllTemplates(FileManager.templatesPath().resolve(context.getArgument("path", String.class))))) {
                        map.put(pos, Utility.makeTemplate(template));
                        pos = dev.findFreePlacePos(pos.west(2));
                    }
                    CodeClient.currentAction = new PlaceTemplates(map, Commands::actionCallback);
                    return 0;
                }
                Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                return -1;
            } catch (Exception e) {
                Utility.sendMessage(Text.translatable("codeclient.files.template_fail"), ChatType.FAIL);
                return -2;
            }
        })));
        dispatcher.register(literal("save").then(argument("path", StringArgumentType.greedyString()).suggests(Commands::suggestDirectories).executes(context -> {
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
        })));
        dispatcher.register(literal("load").then(argument("path", StringArgumentType.greedyString()).suggests(Commands::suggestTemplates).executes(context -> {
            if (CodeClient.MC.player.isCreative()) {
                String arg = context.getArgument("path", String.class);
                Path path = FileManager.templatesPath().resolve(arg + ".dft");
                if (Files.notExists(path)) {
                    Utility.sendMessage(Text.translatable("codeclient.files.error.cant_read"), ChatType.FAIL);
                    return -1;
                }
                try {
                    byte[] data = Files.readAllBytes(path);
                    ItemStack template = Utility.makeTemplate(new String(Base64.getEncoder().encode(data)));
                    template.setCustomName(Text.empty().formatted(Formatting.RED).append("Saved Template").append(Text.literal(" » ").formatted(Formatting.DARK_RED, Formatting.BOLD)).append(String.valueOf(FileManager.templatesPath().relativize(path))));
                    CodeClient.MC.player.giveItemStack(template);
                    Utility.sendInventory();
                } catch (Exception e) {
                    Utility.sendMessage(Text.translatable("codeclient.files.error.read_file", path), ChatType.FAIL);
                    return -2;
                }
                return 0;
            }
            Utility.sendMessage(Text.translatable("codeclient.warning.creative_mode"), ChatType.FAIL);
            return -1;
        })));
        dispatcher.register(literal("swap").then(argument("path", StringArgumentType.greedyString()).suggests(Commands::suggestTemplates).executes(context -> {
            Path path = FileManager.templatesPath().resolve(context.getArgument("path", String.class));
            try {
                ArrayList<ItemStack> map = new ArrayList<>();
                for (var template : Objects.requireNonNull(getAllTemplates(path))) {
                    map.add(Utility.makeTemplate(template));
                }
                confirm = Utility.createSwapper(map, Commands::actionCallback).swap();
                Utility.sendMessage(Text.translatable("codeclient.action.confirmcc.use"), ChatType.INFO);
            } catch (IOException e) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.read_file", path), ChatType.FAIL);
            }
            return 0;
        })));
        dispatcher.register(literal("delete").then(argument("path", StringArgumentType.greedyString()).suggests(Commands::suggestTemplates).executes(context -> {
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
        })));


        dispatcher.register(literal("jumptofreespot").executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                CodeClient.currentAction = new GoTo(dev.findFreePlacePos(CodeClient.MC.player.getBlockPos()).toCenterPos().add(0, -0.5, 0), Commands::actionCallback);
                CodeClient.currentAction.init();
                return 0;
            }
            return -1;
        }));


        dispatcher.register(literal("confirmcc").executes(context -> {
            if (confirm == null) {
                Utility.sendMessage(Text.translatable("codeclient.action.confirmcc.nothing"), ChatType.INFO);
                return 0;
            }
            if (!(CodeClient.currentAction instanceof None)) {
                Utility.sendMessage(Text.translatable("codeclient.action.busy").append(" ").append(Text.translatable("codeclient.action.abort")), ChatType.FAIL);
                return -1;
            }
            Utility.sendMessage(Text.translatable("codeclient.action.confirmcc.confirm"), ChatType.SUCCESS);
            CodeClient.currentAction = confirm;
            CodeClient.currentAction.init();
            confirm = null;
            return 1;
        }));
    }

    private static CompletableFuture<Suggestions> suggestDirectories(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        return suggestTemplates(context, builder, false);
    }

    private static CompletableFuture<Suggestions> suggestTemplates(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
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

    private static List<String> getAllTemplates(Path path) throws IOException {
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
