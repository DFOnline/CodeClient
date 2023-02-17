package dev.dfonline.codeclient.websocket;

import dev.dfonline.codeclient.CodeClient;
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
        SocketHandler.setConnection(conn);
        CodeClient.LOGGER.info(conn.getRemoteSocketAddress().toString() + " has just connected to the CodeClient API.");
        if(CodeClient.MC.player != null) {
            CodeClient.MC.player.sendMessage(Text.of("§eAn application has connected to CodeClient"));
            CodeClient.MC.player.sendMessage(Text.of("§eRun §c/auth§e to allow it to§l freely modify your plot"));
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        CodeClient.LOGGER.info(conn.getRemoteSocketAddress().toString() + " has just disconnected from the CodeClient API.");
        SocketHandler.setConnection(null);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String response = SocketHandler.onMessage(message);
        if(response != null) conn.send(response);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        CodeClient.LOGGER.info("CodeClient API started!");
    }
}
