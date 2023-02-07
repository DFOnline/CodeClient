package dev.dfonline.codeclient.websocket;

import java.net.InetSocketAddress;
import java.util.Objects;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.ClearPlot;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import org.java_websocket.WebSocket;

public class SocketHandler {
    public static final int PORT = 31375;
    private static WebSocket connection;
    private static boolean authorised = false;
    private static Action action = Action.NONE;

    public static void start() {
        SocketServer websocket = new SocketServer(new InetSocketAddress("localhost", PORT));

        try {
            new Thread(websocket, "CodeClient-API").start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setAuthorised(boolean isAuthorised) {
        authorised = isAuthorised;
        if(isAuthorised) connection.send("{\"status\":\"auth\",\"message\":\"This app was just authorized from the game!\"}");
    }

    public static void setConnection(WebSocket socket) {
        action = null;
        if(connection != null) {
            connection.send("Connection was closed because another one was opened at " + connection.getRemoteSocketAddress());
        }
        connection = socket;
    }

    public static String onMessage(String message) {
        if(!authorised) return "{\"status\":\"error\",\"error\":\"auth\",\"message\":\"This app isn't authorized, check you have run /auth in the game.\"}";
        if(action == Action.CLEAR) return "{\"status\":\"error\",\"error\":\"clearing\",\"message\":\"The client is already clearing a plot. If it is stuck, run /abort and /auth.\"}";
        if(action == Action.SPAWN) return "{\"status\":\"error\",\"error\":\"spawn\",\"message\":\"The client is trying to go to the plot spawn. If it is stuck, run /abort and /auth.\"}";
        if(Objects.equals(message, "clear")) {
            action = Action.CLEAR;
            CodeClient.currentAction = new ClearPlot(() -> {
                connection.send("{\"status\":\"action\",\"action\":\"clear\",\"progress\":\"finish\",\"message\":\"Finished clear plot.\"}");
                action = Action.NONE;
            });
            CodeClient.currentAction.init();
            return "{\"status\":\"action\",\"action\":\"clear\",\"progress\":\"start\",\"message\":\"Starting clear plot.\"}";
        }
        if(Objects.equals(message, "spawn")) {
            action = Action.SPAWN;
            CodeClient.currentAction = new MoveToSpawn(() -> {
                connection.send("{\"status\":\"spawn\",\"action\":\"clear\",\"progress\":\"finish\",\"message\":\"Finished move to spawn.\"}");
                action = Action.NONE;
            });
            return "{\"status\":\"action\",\"action\":\"spawn\",\"progress\":\"start\",\"message\":\"Starting move to spawn.\"}";
        }
        return null;
    }

    private enum Action {
        NONE,
        CLEAR,
        SPAWN,
        TEMPLATES,
    }
}
