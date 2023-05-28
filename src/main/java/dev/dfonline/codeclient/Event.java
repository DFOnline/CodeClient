package dev.dfonline.codeclient;

import dev.dfonline.codeclient.location.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.s2c.play.*;

public class Event {
    private static double x;
    private static double z;
    private static Sequence step = Sequence.WAIT_FOR_CLEAR;

    public static <T extends PacketListener> void handlePacket(Packet<T> packet) {
        if(packet instanceof ClearTitleS2CPacket clear) {
            if (clear.shouldReset()) step = Sequence.WAIT_FOR_POS;
        }
        if(packet instanceof PlayerPositionLookS2CPacket pos) {
            x = pos.getX();
            z = pos.getZ();
            if(step == Sequence.WAIT_FOR_POS) step = Sequence.WAIT_FOR_MESSAGE;
        }
        if(packet instanceof OverlayMessageS2CPacket overlay) {
            if (step == Sequence.WAIT_FOR_MESSAGE && overlay.getMessage().getString().startsWith("DiamondFire - ")) {
                updateLocation(new Spawn());
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
        if(packet instanceof GameMessageS2CPacket message) {
            if(step == Sequence.WAIT_FOR_MESSAGE) {
                String content = message.content().getString();
                if(content.equals("» You are now in dev mode.")) {
                    updateLocation(new Dev(x,z));
                }
                if(content.equals("» You are now in build mode.")) {
                    updateLocation(new Build());
                }
                if(content.startsWith("» Joined game: ") && content.endsWith(".")) {
                    updateLocation(new Play());
                }
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
        if(packet instanceof GameJoinS2CPacket) {
            updateLocation(new Spawn());
        }
    }

    public static void updateLocation(Location location) {
        CodeClient.lastLocation = CodeClient.location;
        CodeClient.location = location;
        CodeClient.LOGGER.info("Changed location: " + location.name());
    }

    private enum Sequence {
        WAIT_FOR_CLEAR,
        WAIT_FOR_POS,
        WAIT_FOR_MESSAGE,
    }
}
