package dev.dfonline.codeclient.websocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.GetPlotSize;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
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
        if(socket != null) actionQueue.clear();
        connection = socket;
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
        String content = arguments.length > 1 ? message.substring(arguments[0].length() + 1) : "";
        if(topAction != null && arguments.length > 1 && Objects.equals(topAction.name, arguments[0])) {
            topAction.message(connection,content);
            return;
        }
        switch (arguments[0]) {
            case "clear" -> SocketHandler.actionQueue.add(new Clear());
            case "spawn" -> SocketHandler.actionQueue.add(new Spawn());
            case "size" -> SocketHandler.actionQueue.add(new Size());
            case "place" -> SocketHandler.actionQueue.add(new Place());
            case "inv" -> SocketHandler.actionQueue.add(new SendInventory());
            case "setinv" -> SocketHandler.actionQueue.add(new SetInventory(content));
            case "give" -> SocketHandler.actionQueue.add(new Give(content));
            default -> connection.send("invalid");
        }
        Action firstAction = actionQueue.get(0);
        if(firstAction == null) return;
        if(firstAction.active) return;
        next();
    }

    private static Action getTopAction() {
        if(actionQueue.isEmpty()) return null;
        return actionQueue.get(actionQueue.size() - 1);
    }

    private static void next() {
        if(actionQueue.isEmpty()) return;
        Action firstAction = actionQueue.get(0);
        if(firstAction == null) return;
        if(firstAction.active) {
            actionQueue.remove(0);
            CodeClient.LOGGER.info(firstAction.name + " done");
            next();
            return;
        }
        CodeClient.LOGGER.info("starting " + firstAction.name);
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
                    if(responder.isOpen()) responder.send(plot.getSize().name());
                    next();
                }
                else {
                    CodeClient.currentAction = new GetPlotSize(() -> {
                        if(responder.isOpen()) responder.send(plot.getSize().name());
                        next();
                    });
                }
            }
        }

        @Override
        public void message(WebSocket responder, String message) {}
    }
    private static class Place extends SocketHandler.Action {
        private interface CreatePlacer {
            PlaceTemplates run(ArrayList<ItemStack> templates, WebSocket responder);
        }

        private enum Method {
            DEFAULT((ArrayList<ItemStack> templates, WebSocket responder) -> Utility.createPlacer(templates, () -> {
                CodeClient.currentAction = new None();
                if(responder.isOpen()) responder.send("place done");
                next();})),
            COMPACT((ArrayList<ItemStack> templates, WebSocket responder) -> Utility.createPlacer(templates, () -> {
                CodeClient.currentAction = new None();
                if(responder.isOpen()) responder.send("place done");
                next();},true)),
            SWAP((ArrayList<ItemStack> templates, WebSocket responder) -> Utility.createSwapper(templates, () -> {
                CodeClient.currentAction = new None();
                if(responder.isOpen()) responder.send("place done");
                next();})),
            ;

            public final CreatePlacer createPlacer;
            Method(CreatePlacer createPlacer) {
                this.createPlacer = createPlacer;
            }
        }
        private final ArrayList<ItemStack> templates = new ArrayList<>();
        private Method method = Method.DEFAULT;
        public boolean ready = false;

        Place() {
            super("place");
        }

        @Override public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            if(!ready) return;
            var placer = method.createPlacer.run(templates,responder);
            if(placer == null) return;
            CodeClient.currentAction = placer;
            CodeClient.currentAction.init();
        }

        @Override
        public void message(WebSocket responder, String message) {
            if(message.equals("compact")) {
                this.method = Method.COMPACT;
                return;
            }
            if(message.equals("swap")) {
                this.method = Method.SWAP;
                return;
            }
            if(message.equals("go")) {
                this.ready = true;
                if(Objects.equals(actionQueue.get(0), this)) {
                    this.start(responder);
                }
                return;
            }
            templates.add(Utility.makeTemplate(message));
        }
    }
    private static class SendInventory extends SocketHandler.Action {
        SendInventory() {
            super("inv");
        }

        @Override
        public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            NbtCompound nbt = new NbtCompound();
            CodeClient.MC.player.writeNbt(nbt);
            responder.send(String.valueOf(nbt.get("Inventory")));
            next();
        }

        @Override
        public void message(WebSocket responder, String message) {}
    }
    private static class SetInventory extends SocketHandler.Action {
        private final String content;

        SetInventory(String content) {
            super("setinv");
            this.content = content;
        }

        @Override
        public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            if(!CodeClient.MC.player.isCreative()) {
                responder.send("not creative mode");
                next();
                return;
            }
            try {
                NbtCompound nbt = new NbtCompound();
                CodeClient.MC.player.writeNbt(nbt);
                nbt.put("Inventory",StringNbtReader.parse("{Inventory:" + content + "}").getList("Inventory", NbtElement.COMPOUND_TYPE));
                CodeClient.MC.player.readNbt(nbt);
                Utility.sendInventory();
            } catch (CommandSyntaxException e) {
                responder.send("invalid nbt");
            }
            finally {
                next();
            }
        }

        @Override
        public void message(WebSocket responder, String message) {}
    }
    private static class Give extends SocketHandler.Action {
        private final String content;

        Give(String content) {
            super("give");
            this.content = content;
        }

        @Override
        public void set(WebSocket responder) {}

        @Override
        public void start(WebSocket responder) {
            if(!CodeClient.MC.player.isCreative()) {
                responder.send("not creative mode");
                next();
                return;
            }
            try {
                CodeClient.MC.player.giveItemStack(ItemStack.fromNbt(StringNbtReader.parse(content)));
                Utility.sendInventory();
            } catch (CommandSyntaxException e) {
                responder.send("invalid nbt");
            }
            finally {
                next();
            }
        }

        @Override
        public void message(WebSocket responder, String message) {}
    }
}
