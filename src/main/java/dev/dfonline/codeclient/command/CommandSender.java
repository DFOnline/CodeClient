package dev.dfonline.codeclient.command;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.RateLimiter;

import java.util.ArrayDeque;

/**
 * Queues up commands and sends them to avoid getting kicked for spam.
 */
public class CommandSender {
    // Vanilla Minecraft uses 20 increment 200 threshold.
    // We have a lower threshold for extra safety and to account for lag.
    private static final RateLimiter rateLimiter = new RateLimiter(20, 140);
    private static final ArrayDeque<String> commandQueue = new ArrayDeque<>();

    /*
    public static void queue(String command, int delay, Runnable callback) {
        rateLimiter.increment();
        for (int i = 0; i < delay; i++) {
        }
        commandQueue.add(command);
        if (callback != null) {
            callback.run();
        }
    }

    public static void queue(String command, int delay) {
        queue(command, delay, null);
    }

    public static void queue(String command) {
        queue(command, 0, null);
    }
    */

    public static void queue(String command) {
        rateLimiter.increment();
        commandQueue.add(command);
    }

    public static void clearQueue() {
        commandQueue.clear();
    }

    public static int queueSize() {
        return commandQueue.size();
    }


    public static void tick() {
        rateLimiter.tick();
        if (CodeClient.MC.getNetworkHandler() == null) return;
        if (!rateLimiter.isRateLimited() && !commandQueue.isEmpty()) {
            CodeClient.MC.getNetworkHandler().sendCommand(commandQueue.pop());
            // No need to increment here, since our packet listener will do that for us. (Event#onSendPacket)
        }
    }


    /**
     * Registers a command send.
     * This should be called whenever a command or chat message is sent to the server.
     */
    public static void registerCommandSend() {
        rateLimiter.increment();
    }

}