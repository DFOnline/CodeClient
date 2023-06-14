package dev.dfonline.codeclient.websocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.GetPlotSize;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.location.Plot;
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
        JsonObject response = new JsonObject();
        if(!authorised) {
            response.addProperty("status","error");
            response.addProperty("error","auth");
            response.addProperty("message","This app isn't authorized, check you have run /auth in the game.");
            return response.toString();
        }
        if(action == Action.CLEAR) {
            response.addProperty("status","error");
            response.addProperty("error","clear");
            response.addProperty("message","The client is already clearing a plot. If it is stuck, run /abort.");
            return response.toString();
        }
        if(action == Action.SPAWN) {
            response.addProperty("status","error");
            response.addProperty("error","spawn");
            response.addProperty("message","The client is trying to go to the plot spawn. If it is stuck, run /abort.");
            return response.toString();
        }
        String[] arguments = message.split(" ");
        switch (arguments[0]) {
            case "clear" -> {
                action = Action.CLEAR;
                response.addProperty("status","action");
                response.addProperty("action","clear");
                CodeClient.currentAction = new ClearPlot(() -> {
                    response.addProperty("progress","finish");
                    response.addProperty("message","Finished clear plot.");
                    connection.send(response.toString());
                    action = Action.NONE;
                });
                CodeClient.currentAction.init();
                response.addProperty("progress","start");
                response.addProperty("message","Starting clear plot.");
                return response.toString();
            }
            case "spawn" -> {
                action = Action.SPAWN;
                response.addProperty("status","action");
                response.addProperty("action","spawn");
                CodeClient.currentAction = new MoveToSpawn(() -> {
                    response.addProperty("progress","finish");
                    response.addProperty("message","Finished move to spawn.");
                    connection.send(response.toString());
                    action = Action.NONE;
                });
                CodeClient.currentAction.init();
                response.addProperty("progress","start");
                response.addProperty("message","Starting move to spawn.");
                return response.toString();
            }
            case "size" -> {
                action = Action.SIZE;
                response.addProperty("status","action");
                response.addProperty("action","size");
                CodeClient.currentAction = new GetPlotSize(() -> {
                    response.addProperty("progress","finish");
                    response.addProperty("message","Finished get plot size.");
                    if(CodeClient.location instanceof Plot plot) {
                        response.addProperty("data",plot.getSize().name());
                    }
                    connection.send(response.toString());
                    action = Action.NONE;
                });
                CodeClient.currentAction.init();
                response.addProperty("progress","start");
                response.addProperty("message","Starting get plot size.");
                return response.toString();
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
                response.addProperty("status","error");
                response.addProperty("error","swap");
                response.addProperty("message","Not implemented.");
                return response.toString();
            }
            default -> {
                response.addProperty("status","error");
                response.addProperty("error","generic");
                response.addProperty("message","Invalid command.");
                return response.toString();
            }
        }
        return null;
    }

    private enum Action {
        NONE,
        CLEAR, // Clearing a plot
        SPAWN, // Moving client to plot origin
        SIZE, // Getting plot size
        TEMPLATES, // Collecting templates for PLACE
        PLACE, // Aforementioned place, places collected templates into plot
    }
}
