package dev.dfonline.codeclient.websocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.java_websocket.WebSocket;

public class SocketHandler {
    public static final int PORT = 31375;
    private static WebSocket connection = null;
    private static boolean authorised = false;
    private static Action action = Action.NONE;
    private static final ArrayList<ItemStack> templates = new ArrayList<>();

    public static void start() {
        SocketServer websocket = new SocketServer(new InetSocketAddress("localhost", PORT));

        try {
            new Thread(websocket, "CodeClient-API").start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAuthorised(boolean isAuthorised) {
        action = Action.NONE;
        authorised = isAuthorised;
        if(isAuthorised) connection.send("{\"status\":\"auth\",\"message\":\"This app was just authorized from the game!\"}");
    }

    public static void setConnection(WebSocket socket) {
        action = Action.NONE;
        connection = socket;
    }

    public static String onMessage(String message) {
        if(!authorised) return "{\"status\":\"error\",\"error\":\"auth\",\"message\":\"This app isn't authorized, check you have run /auth in the game.\"}";
        if(action == Action.CLEAR) return "{\"status\":\"error\",\"error\":\"clearing\",\"message\":\"The client is already clearing a plot. If it is stuck, run /abort and /auth.\"}";
        if(action == Action.SPAWN) return "{\"status\":\"error\",\"error\":\"spawn\",\"message\":\"The client is trying to go to the plot spawn. If it is stuck, run /abort and /auth.\"}";
        String[] arguments = message.split(" ");
        switch (arguments[0]) {
            case "clear" -> {
                action = Action.CLEAR;
                CodeClient.currentAction = new ClearPlot(() -> {
                    connection.send("{\"status\":\"action\",\"action\":\"clear\",\"progress\":\"finish\",\"message\":\"Finished clear plot.\"}");
                    action = Action.NONE;
                });
                CodeClient.currentAction.init();
                return "{\"status\":\"action\",\"action\":\"clear\",\"progress\":\"start\",\"message\":\"Starting clear plot.\"}";
            }
            case "spawn" -> {
                action = Action.SPAWN;
                CodeClient.currentAction = new MoveToSpawn(() -> {
                    connection.send("{\"status\":\"action\",\"action\":\"spawn\",\"progress\":\"finish\",\"message\":\"Finished move to spawn.\"}");
                    action = Action.NONE;
                });
                CodeClient.currentAction.init();
                return "{\"status\":\"action\",\"action\":\"spawn\",\"progress\":\"start\",\"message\":\"Starting move to spawn.\"}";
            }
            case "place" -> {
                if(action != Action.TEMPLATES) {
                    CodeClient.LOGGER.info("Starting place operation");
                    action = Action.TEMPLATES;
                    templates.clear();
                    return "{\"status\":\"action\",\"action\":\"place\",\"progress\":\"start\",\"message\":\"Starting place operation.\"}";
                }
                if(arguments.length == 2) {
                    CodeClient.LOGGER.info("Added " + templates.size() + " to the operation");
                    ItemStack template = new ItemStack(Items.ENDER_CHEST);
                    NbtCompound nbt = new NbtCompound();
                        NbtCompound PublicBukkitValues = new NbtCompound();
                        PublicBukkitValues.putString("hypercube:codetemplatedata","{\"author\":\"CodeClient\",\"name\":\"Template to be placed\",\"version\":1,\"code\":\"" + arguments[1] + "\"}");
                    nbt.put("PublicBukkitValues", PublicBukkitValues);
                    template.setNbt(nbt);
                    templates.add(template);
                    return "{\"status\":\"action\",\"action\":\"place\",\"progress\":\"active\",\"message\":\"Added template " + templates.size() + ".\"}";
                }
                if(arguments.length == 1) {
                    CodeClient.currentAction = new PlaceTemplates(templates, () -> {
                        connection.send("{\"status\":\"spawn\",\"action\":\"place\",\"progress\":\"finish\",\"message\":\"Finished placing templates.\"}");
                        templates.clear();
                        action = Action.NONE;
                    });
                    CodeClient.currentAction.init();
                    action = Action.PLACE;
                }
            }
            case "swap" -> {
                return "{\"status\":\"error\",\"error\":\"swapping\",\"message\":\"Not implemented.\"}";
            }
            default -> {
                return "{\"status\":\"error\",\"error\":\"generic\",\"message\":\"Invalid command.\"}";
            }
        }
        return null;
    }

    private enum Action {
        NONE,
        CLEAR, // Clearing a plot
        SPAWN, // Moving client to plot origin
        TEMPLATES, // Collecting templates for PLACE
        PLACE, // Aforementioned place, places collected templates into plot
    }
}
