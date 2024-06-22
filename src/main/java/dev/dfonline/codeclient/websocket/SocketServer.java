package dev.dfonline.codeclient.websocket;

import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import net.minecraft.text.Text;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class SocketServer extends WebSocketServer {
    public SocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        CodeClient.LOGGER.info("connection");
        SocketHandler.setConnection(conn);
        CodeClient.LOGGER.info(conn.getRemoteSocketAddress().toString() + " has just connected to the CodeClient API.");
        Utility.sendMessage(Text.translatable("codeclient.api.connect"), ChatType.INFO);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        CodeClient.LOGGER.info(conn.getRemoteSocketAddress().toString() + " has just disconnected from the CodeClient API.");
        SocketHandler.setConnection(null);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (conn == null) return;
        SocketHandler.onMessage(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        CodeClient.LOGGER.info("CodeClient API started!");
    }
}
