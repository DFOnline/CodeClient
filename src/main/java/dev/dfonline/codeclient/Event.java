package dev.dfonline.codeclient;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class Event {
    private static double x;
    private static double z;
    private static Sequence step = Sequence.WAIT_FOR_CLEAR;

    public static <T extends PacketListener> void handlePacket(Packet<T> packet) {
        if(packet instanceof ClientboundClearTitlesPacket clear) {
            if (clear.shouldResetTimes()) step = Sequence.WAIT_FOR_POS;
        }
        if(packet instanceof ClientboundPlayerPositionPacket pos) {
            x = pos.getX();
            z = pos.getZ();
            if(step == Sequence.WAIT_FOR_POS) step = Sequence.WAIT_FOR_MESSAGE;
        }
        if(packet instanceof ClientboundSetActionBarTextPacket overlay) {
            if (step == Sequence.WAIT_FOR_MESSAGE && overlay.getText().getString().startsWith("DiamondFire - ")) {
                CodeClient.LOGGER.info("Spawn mode.");
                CodeClient.worldPlot = null;
                PlotLocation.set(0,0,0);
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
        if(packet instanceof ClientboundSystemChatPacket message) {
            if(step == Sequence.WAIT_FOR_MESSAGE) {
                String content = message.content().getString();
                if(content.equals("» You are now in dev mode.")) {
                    CodeClient.LOGGER.info("Dev mode.");
                    PlotLocation.set(x + 9.5, 50, z - 10.5);
                }
                if(content.equals("» You are now in build mode.")) {
                    CodeClient.LOGGER.info("Build mode.");
                }
                if(content.startsWith("» Joined game: ") && content.endsWith(".")) {
                    CodeClient.LOGGER.info("Play mode.");
                }
                step = Sequence.WAIT_FOR_CLEAR;
            }
        }
    }

    private enum Sequence {
        WAIT_FOR_CLEAR,
        WAIT_FOR_POS,
        WAIT_FOR_MESSAGE,
    }
}
