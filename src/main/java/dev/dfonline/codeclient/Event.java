package dev.dfonline.codeclient;

import dev.dfonline.codeclient.location.Build;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Play;
import dev.dfonline.codeclient.location.Spawn;
import net.minecraft.network.Packet;
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
                CodeClient.LOGGER.info("Spawn mode.");
                CodeClient.worldPlot = null;
                CodeClient.location = new Spawn();
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
        if(packet instanceof GameMessageS2CPacket message) {
            if(step == Sequence.WAIT_FOR_MESSAGE) {
                String content = message.content().getString();
                if(content.equals("» You are now in dev mode.")) {
                    CodeClient.LOGGER.info("Dev mode.");
                    CodeClient.location = new Dev(x,z);
                }
                if(content.equals("» You are now in build mode.")) {
                    CodeClient.LOGGER.info("Build mode.");
                    CodeClient.location = new Build();
                }
                if(content.startsWith("» Joined game: ") && content.endsWith(".")) {
                    CodeClient.LOGGER.info("Play mode.");
                    CodeClient.location = new Play();
                }
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
        if(packet instanceof GameJoinS2CPacket) {
            CodeClient.location = new Spawn();
            CodeClient.LOGGER.info("Spawn mode.");
        }
    }

    private enum Sequence {
        WAIT_FOR_CLEAR,
        WAIT_FOR_POS,
        WAIT_FOR_MESSAGE,
    }
}
