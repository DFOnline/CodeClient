package dev.dfonline.codeclient.websocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Objects;

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
    private static final ArrayList<Action> actionQueue = new ArrayList<>();
    private static SocketServer websocket;
    private static Thread socketThread;

    public static void start() {
        try {
            websocket = new SocketServer(new InetSocketAddress("localhost", PORT));
            socketThread = new Thread(websocket, "CodeClient-API");
            socketThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        try {
            websocket.stop();
        } catch (Exception ignored) {}
    }

    public static void setAuthorised(boolean isAuthorised) {
        actionQueue.clear();
        authorised = isAuthorised;
        if(isAuthorised) connection.send("auth");
    }

    public static void setConnection(WebSocket socket) {
        connection = socket;
        actionQueue.clear();
    }

    public static void onMessage(String message) {
        JsonObject response = new JsonObject();
        if(!authorised) {
            connection.send("noauth");
            return;
        }
        String[] arguments = message.split(" ");
        Action topAction = getTopAction();
        if(arguments[0] == null) return;
        if(topAction != null && arguments.length > 1 && Objects.equals(topAction.name, arguments[0])) {
            topAction.message(connection,message.substring(arguments[0].length() + 1));
            return;
        }
        switch (arguments[0]) {
            case "clear" -> {
                SocketHandler.actionQueue.add(new Clear());
            }
            case "spawn" -> {
                SocketHandler.actionQueue.add(new Spawn());
            }
            case "size" -> {
                SocketHandler.actionQueue.add(new Size());
            }
            case "place" -> {
                SocketHandler.actionQueue.add(new Place());
            }
            case "swap" -> {
                response.addProperty("status","error");
                response.addProperty("error","swap");
                response.addProperty("message","Not implemented.");
                connection.send(response.toString());
            }
            default -> {
                connection.send("invalid");
            }
        }
        Action firstAction = actionQueue.get(0);
        if(firstAction == null) return;
        if(firstAction.active) return;
        next();
    }

    private static Action getTopAction() {
        if(actionQueue.size() == 0) return null;
        return actionQueue.get(actionQueue.size() - 1);
    }

    private static void next() {
        if(actionQueue.size() == 0) return;
        Action firstAction = actionQueue.get(0);
        if(firstAction == null) return;
        if(firstAction.active) {
            actionQueue.remove(0);
            next();
            return;
        }
        firstAction.active = true;
        firstAction.start(connection);
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

    private static class Clear extends SocketHandler.Action {
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
    private static class Spawn extends SocketHandler.Action {
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
    private static class Size extends SocketHandler.Action {
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
    private static class Place extends SocketHandler.Action {
        private static final ArrayList<ItemStack> templates = new ArrayList<>();

        Place() {
            super("place");
        }

        @Override public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            if(templates.size() == 0) return;
            CodeClient.currentAction = new PlaceTemplates(templates, () -> {
                responder.send("place done");
                next();
            });
            CodeClient.currentAction.init();
        }

        @Override
        public void message(WebSocket responder, String message) {
            if(message.equals("go")) {
                this.start(responder);
            }

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
