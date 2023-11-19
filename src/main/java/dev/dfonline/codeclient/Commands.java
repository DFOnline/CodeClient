package dev.dfonline.codeclient;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.*;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.BuildClip;
import dev.dfonline.codeclient.dev.LastPos;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.location.*;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("auth").executes(context -> {
            SocketHandler.setAuthorised(true);
            Utility.sendMessage("The connect app has been authorised,§l it can now do anything to your plot code.", ChatType.SUCCESS);
            Utility.sendMessage("You can remove the app by running §e/auth remove", ChatType.INFO);
            return 0;
        }).then(literal("remove").executes(context -> {
            SocketHandler.setAuthorised(false);
            Utility.sendMessage("The connected app is no longer authorised, which might break it.", ChatType.SUCCESS);
            return 0;
        })).then(literal("disconnect").executes(context -> {
            Utility.sendMessage("Not implemented.", ChatType.FAIL);
            SocketHandler.setConnection(null);
            return 0;
        })));


        dispatcher.register(literal("worldplot").executes(context -> {
            if(CodeClient.location instanceof Plot plot) plot.setSize(null);
            return 0;
        }).then(literal("basic").executes(context -> {
            if(CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.BASIC);
            return 0;
        })).then(literal("large").executes(context -> {
            if(CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.LARGE);
            return 0;
        })).then(literal("massive").executes(context -> {
            if(CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.MASSIVE);
            return 0;
        })));


        dispatcher.register(literal("fixcc").executes(context -> {
            CodeClient.currentAction = new None();
            CodeClient.location = null;
            BuildClip.disableClipping();
            SocketHandler.setConnection(null);
            ActionDump.clear();
            Config.clear();
            return 0;
        }));


        dispatcher.register(literal("abort").executes(context -> {
            CodeClient.currentAction = new None();
            return 0;
        }));


        dispatcher.register(literal("getactiondump").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.NONE, () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        }).then(literal("section").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.SECTION, () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        })).then(literal("ampersand").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.AMPERSAND, () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
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
                Path path = FileManager.writeFile("widthdump.txt",dataFinal);
                Utility.sendMessage(Text.literal("Written to " + path).setStyle(Text.empty().getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toString()))),ChatType.SUCCESS);
            }
            catch (Exception ignored) {
                Utility.sendMessage("Couldn't save to a file. It has been logged into the console.",ChatType.FAIL);
                CodeClient.LOGGER.info(dataFinal);
            }
            return 0;
        }));

        dispatcher.register(literal("back").executes(context -> {
            if(CodeClient.location instanceof Creator plot) {
                if(plot.devPos == null) {
                    Utility.sendMessage("There is no position to go back to!", ChatType.FAIL);
                    return 1;
                }
                if(!(CodeClient.currentAction instanceof None)) {
                    Utility.sendMessage("CodeClient is currently busy, try again in a moment.",ChatType.FAIL);
                    return 1;
                }
                if(LastPos.tpBack()) return 0;
                else {
                    Utility.sendMessage("An error occurred whilst trying to go back.",ChatType.FAIL);
                    return 1;
                }
            }
            else {
                Utility.sendMessage("You must be in dev or build mode to do this!",ChatType.FAIL);
                return 1;
            }
        }));

        dispatcher.register(literal("getspawn").executes(context -> {
            if(!(CodeClient.location instanceof Dev)) return 1;
            CodeClient.currentAction = new MoveToSpawn(() -> Utility.sendMessage("Done!", ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        }));
        dispatcher.register(literal("getsize").executes(context -> {
            if(!(CodeClient.location instanceof Dev)) return 1;
            CodeClient.currentAction = new GetPlotSize(() -> {
                CodeClient.currentAction = new None();
                Utility.sendMessage("Done!");
            });
            CodeClient.currentAction.init();
            return 0;
        }));
//        dispatcher.register(literal("clearplot").executes(context -> {
//            CodeClient.currentAction = new ClearPlot(() -> Utility.sendMessage("Done!", ChatType.SUCCESS));
//            CodeClient.currentAction.init();
//            return 0;
//        }));
//        dispatcher.register(literal("placetemplate").executes(context -> {
//            CodeClient.currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> {
//                Utility.sendMessage("Done!", ChatType.SUCCESS);
//                CodeClient.currentAction = new None();
//            });
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

        dispatcher.register(literal("scanfor").then(argument("name", StringArgumentType.greedyString()).executes(context -> {
            if(CodeClient.location instanceof Dev dev) {
                Pattern pattern = Pattern.compile(context.getArgument("name", String.class), Pattern.CASE_INSENSITIVE);
                var scan = dev.scanForSigns(pattern);
                Utility.sendMessage("Scan results: ");
                for (var res: scan.entrySet()) {
                    Utility.sendMessage("- " + res.getKey() + ": " + res.getValue().getMessage(1,false).getString());
                }
                return 0;
            }
            Utility.sendMessage("Couldn't scan.", ChatType.FAIL);
            return -1;
        })));
        dispatcher.register(literal("swapininv").executes(context -> {
            if(CodeClient.location instanceof Dev) {
                PlaceTemplates action = Utility.createSwapper(Utility.templatesInInventory(), () -> {
                    CodeClient.currentAction = new None();
                    Utility.sendMessage("Done!", ChatType.SUCCESS);
                });
                if(action == null) return -2;
                CodeClient.currentAction = action.swap();
                CodeClient.currentAction.init();
                return 0;
            }
            return -1;
        }));
        dispatcher.register(literal("placetemplates").executes(context -> {
            if(CodeClient.location instanceof Dev dev) {
                var map = new HashMap<BlockPos, ItemStack>();
                BlockPos pos = dev.findFreePlacePos();
                for (var template: Utility.templatesInInventory()) {
                    map.put(pos, template);
                    pos = dev.findFreePlacePos(pos.west(2));
                }
                CodeClient.currentAction = new PlaceTemplates(map, () -> {
                    CodeClient.currentAction = new None();
                    Utility.sendMessage("Done!", ChatType.SUCCESS);
                });
                return 0;
            }
            Utility.sendMessage("You must be in dev mode!",ChatType.FAIL);
            return -1;
        }));
        dispatcher.register(literal("save").then(argument("path", StringArgumentType.greedyString()).suggests((context, builder) -> {
            try {
                var possibilities = new ArrayList<String>();

                String[] path =  builder.getRemaining().split("/",-1);
                String currentPath = String.join("/", (Arrays.stream(path).toList().subList(0,path.length - 1)));
                var list = Files.list(FileManager.templatesPath().resolve(currentPath));
                for (var file: list.toList()) {
                    if(Files.isDirectory(file)) possibilities.add((currentPath.isEmpty() ? "" : currentPath + "/") + file.getFileName().toString() + "/");
                }
                list.close();
                for (String possibility: possibilities) {
                    if(possibility.contains(builder.getRemainingLowerCase())) builder.suggest(possibility,Text.literal("Folder"));
                }
            } catch (IOException ignored) {}
            return CompletableFuture.completedFuture(builder.build());
        }).executes(context -> {
            String data = Utility.templateDataItem(CodeClient.MC.player.getMainHandStack());
            if(data == null) {
                Utility.sendMessage("You need to hold a template to save.",ChatType.FAIL);
                return 0;
            }

            String arg = context.getArgument("path",String.class);
            String[] path = arg.split("/");
            Path currentPath = FileManager.templatesPath();
            // Create folders
            for (String dir: Arrays.stream(path).toList().subList(0,path.length - 1)) {
                currentPath = currentPath.resolve(dir);
                if(Files.notExists(currentPath)) {
                    try {
                        Files.createDirectory(currentPath);
                    }
                    catch (Exception ignored) {
                        Utility.sendMessage("Could not make a folder at " + currentPath);
                    }
                }
                else if(!Files.isDirectory(currentPath)) {
                    Utility.sendMessage(currentPath + " isn't a directory, can't put anything inside it!", ChatType.FAIL);
                    return -1;
                }
            }
            currentPath = currentPath.resolve(path[path.length - 1] + ".dft");
            try {
                Files.write(currentPath.resolve(currentPath), Base64.getDecoder().decode(data));
                Utility.sendMessage("Saved " + currentPath);
            }
            catch (Exception e) {
                Utility.sendMessage("Couldn't save the file to: " + currentPath, ChatType.FAIL);
                CodeClient.LOGGER.error(e.getMessage());
            }
            return 0;
        })));
        dispatcher.register(literal("load").then(argument("path", StringArgumentType.greedyString()).suggests((context, builder) -> {
            CodeClient.LOGGER.info("hi");
            try {
                var possibilities = new ArrayList<String>();

                String[] path =  builder.getRemaining().split("/",-1);
                String currentPath = String.join("/", (Arrays.stream(path).toList().subList(0,path.length - 1)));
                var list = Files.list(FileManager.templatesPath().resolve(currentPath));
                for (var file: list.toList()) {
                    String s = currentPath.isEmpty() ? "" : currentPath + "/";
                    if(Files.isDirectory(file)) possibilities.add(s + file.getFileName().toString() + "/");
                    else if(file.getFileName().toString().endsWith(".dft")) {
                        String name = file.getFileName().toString();
                        possibilities.add(s + name.substring(0, name.length() - 4));
                    }
                }
                list.close();
                for (String possibility: possibilities) {
                    if(possibility.contains(builder.getRemainingLowerCase())) builder.suggest(possibility,Text.literal("Folder"));
                }
            } catch (IOException ignored) {}
            return CompletableFuture.completedFuture(builder.build());
            }).executes(context -> {
                String arg = context.getArgument("path",String.class);
                Path path = FileManager.templatesPath().resolve(arg + ".dft");
                if(Files.notExists(path)) {
                    Utility.sendMessage("This template doesn't exist.", ChatType.FAIL);
                    return -1;
                }
                try {
                    byte[] data = Files.readAllBytes(path);
                    Utility.sendMessage(new String(Base64.getEncoder().encode(data)));
                }
                catch (Exception e) {
                    Utility.sendMessage("Couldn't read file.", ChatType.FAIL);
                    return -2;
                }
                return 0;
            }
        )));
        dispatcher.register(literal("jumptofreespot").executes(context -> {
            if(CodeClient.location instanceof Dev dev) {
                CodeClient.currentAction = new GoTo(dev.findFreePlacePos().toCenterPos().add(0,-0.5,0), () -> {
                    CodeClient.currentAction = new None();
                    Utility.sendMessage("Done!", ChatType.SUCCESS);
                });
                CodeClient.currentAction.init();
                return 0;
            }
            return -1;
        }));
    }
}
