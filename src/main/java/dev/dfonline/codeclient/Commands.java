package dev.dfonline.codeclient;

import com.mojang.brigadier.CommandDispatcher;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.*;
import dev.dfonline.codeclient.dev.BuildClip;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import dev.dfonline.codeclient.websocket.SocketHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;

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
            if(CodeClient.location instanceof Plot plot) plot.setSize(Plot.Size.BASIC);
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
            StringBuilder data = new StringBuilder("WIDTHDUMP\nFORMAT GOES AS\n<UNICODE> <WIDTH> <BOLD WIDTH>\n");
            for (int codePoint = Character.MIN_CODE_POINT; codePoint <= Character.MAX_CODE_POINT; codePoint++) {
                String character = new String(Character.toChars(codePoint));
                TextRenderer renderer = CodeClient.MC.textRenderer;
                data.append(character).append(" ").append(renderer.getWidth(character)).append(" ").append(renderer.getWidth(Text.literal(character).formatted(Formatting.BOLD))).append("\n");
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
//            CodeClient.currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> Utility.sendMessage("Done!", ChatType.SUCCESS));
//            CodeClient.currentAction.init();
//            return 0;
//        }));

//        dispatcher.register(literal("codeforme").executes(context -> {
//            if(!(CodeClient.location instanceof Dev)) return 1;
//            CodeClient.currentAction = new ClearPlot(() -> {
//                CodeClient.currentAction = new MoveToSpawn(() -> {
//                    CodeClient.currentAction = new PlaceTemplates(Utility.TemplatesInInventory(), () -> {
//                        Utility.sendMessage("Done!", ChatType.SUCCESS);
//                    });
//                    CodeClient.currentAction.init();
//                });
//                CodeClient.currentAction.init();
//            });
//            CodeClient.currentAction.init();
//            return 0;
//        }));
    }
}
