package dev.dfonline.codeclient;

import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.*;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Detects mode changes.
 */
public class Event {
    private static Vec3d tp;
    private static Sequence step = Sequence.WAIT_FOR_CLEAR;
    private static boolean switchingMode = false;

    public static <T extends PacketListener> void handlePacket(Packet<T> packet) {
        if (packet instanceof ClearTitleS2CPacket clear) {
            if (clear.shouldReset()) step = Sequence.WAIT_FOR_POS;
        }
        if (packet instanceof PlayerPositionLookS2CPacket pos) {
            tp = new Vec3d(pos.change().position().x, pos.change().position().y, pos.change().position().z);
            if (step == Sequence.WAIT_FOR_POS) step = Sequence.WAIT_FOR_MESSAGE;
        }
        if (packet instanceof OverlayMessageS2CPacket overlay) {
            if (step == Sequence.WAIT_FOR_MESSAGE && overlay.text().getString().startsWith("DiamondFire - ")) {
                updateLocation(new Spawn());
            }
        }
        if (packet instanceof GameMessageS2CPacket message) {
            if (step == Sequence.WAIT_FOR_MESSAGE) {
                String content = message.content().getString();
                if (content.equals("» You are now in dev mode.")) {
                    updateLocation(new Dev(tp.x, tp.z));
                }
                if (content.equals("» You are now in build mode.")) {
                    updateLocation(new Build(tp));
                }
                if (content.startsWith("» Joined game: ")) {
                    updateLocation(new Play());
                }
            }
        }
        if (packet instanceof GameJoinS2CPacket) {
            updateLocation(new Spawn());
        }
    }

    public static <T extends PacketListener> void onSendPacket(Packet<T> packet) {
        if (packet instanceof CommandExecutionC2SPacket command) {
            CommandSender.registerCommandSend();
            if (List.of("play", "build", "code", "dev").contains(command.command().replaceFirst("mode ", ""))) {
                switchingMode = true;
            }
        }
        if (packet instanceof ChatMessageC2SPacket) {
            CommandSender.registerCommandSend();
        }
    }

    public static void updateLocation(Location location) {
        CodeClient.lastLocation = CodeClient.location;
        CodeClient.location = location;
        if (switchingMode) {
            switchingMode = false;
            if (location instanceof Plot plot && CodeClient.lastLocation instanceof Plot last)
                plot.copyValuesFrom(last);
            CodeClient.LOGGER.info("Switched location: " + location.name());
        } else CodeClient.LOGGER.info("Changed location: " + location.name());
        step = Sequence.WAIT_FOR_CLEAR;
        CodeClient.LOGGER.info("" + Config.getConfig().InvisibleBlocksInDev);
        CodeClient.onModeChange(location);
        if (Config.getConfig().InvisibleBlocksInDev) CodeClient.shouldReload = true;
    }

    private enum Sequence {
        WAIT_FOR_CLEAR,
        WAIT_FOR_POS,
        WAIT_FOR_MESSAGE,
    }
}
