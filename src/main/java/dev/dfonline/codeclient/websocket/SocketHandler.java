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
    private static ArrayList<Action> ActionQueue;

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
                ActionQueue.add(new Clear());
            }
            case "spawn" -> {

            }
            case "size" -> {

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

                    return "{\"status\":\"action\",\"action\":\"place\",\"progress\":\"active\",\"message\":\"Added template " + templates.size() + ".\"}";
                }
                if(arguments.length == 1) {
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

    private void next() {

    }

    private abstract static class Action {
        boolean active = false;
        public final String name;

        Action(String name) {
            this.name = name;
        }

        /**
         * When the action is added to the queue
         */
        public abstract void set(WebSocket responder);

        /**
         * When the queue reaches the action
         */
        public abstract void start(WebSocket responder);

        /**
         * If a message is sent when this is the last set
         */
        public abstract void message(WebSocket responder, String message);
    }

    private class Clear extends SocketHandler.Action {
        Clear() {super("clear");}
        @Override public void set(WebSocket responder) {}
        @Override public void start(WebSocket responder) {
            CodeClient.currentAction = new ClearPlot(() -> {
                next();
            });
            CodeClient.currentAction.init();
        }
        @Override public void message(WebSocket responder, String message) {}
    }
    private class Spawn extends SocketHandler.Action {
        Spawn() {
            super("spawn");
        }
        @Override public void set(WebSocket responder) {}
        @Override
        public void start(WebSocket responder) {
            CodeClient.currentAction = new MoveToSpawn(() -> {
                next();
            });
            CodeClient.currentAction.init();
        }
        @Override public void message(WebSocket responder, String message) {}
    }
    private class Size extends SocketHandler.Action {
        Size() {
            super("size");
        }

        @Override public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            if(CodeClient.location instanceof Plot plot) {
                if(plot.getSize() != null) {
                    responder.send(plot.getSize().name());
                    next();
                }
                else {
                    CodeClient.currentAction = new GetPlotSize(() -> {
                        responder.send(plot.getSize().name());
                        next();
                    });
                }
            }
        }

        @Override
        public void message(WebSocket responder, String message) {}
    }
    private class Place extends SocketHandler.Action {
        private static final ArrayList<ItemStack> templates = new ArrayList<>();

        Place() {
            super("place");
        }

        @Override public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            CodeClient.currentAction = new PlaceTemplates(templates, () -> {
                next();
            });
            CodeClient.currentAction.init();
        }

        @Override
        public void message(WebSocket responder, String message) {
            ItemStack template = new ItemStack(Items.ENDER_CHEST);
            NbtCompound nbt = new NbtCompound();
            NbtCompound PublicBukkitValues = new NbtCompound();
            PublicBukkitValues.putString("hypercube:codetemplatedata","{\"author\":\"CodeClient\",\"name\":\"Template to be placed\",\"version\":1,\"code\":\"" + message + "\"}");
            nbt.put("PublicBukkitValues", PublicBukkitValues);
            template.setNbt(nbt);
            templates.add(template);
        }
    }
}
