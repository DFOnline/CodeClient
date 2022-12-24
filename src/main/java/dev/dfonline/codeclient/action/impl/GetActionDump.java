package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.util.Date;
import java.util.Objects;

public class GetActionDump extends Action {
    public StringBuilder capturedData = null;
    private boolean doColours;

    private int lines;
    private int length;
    private Date startTime;
    private boolean isDone = false;

    public GetActionDump(boolean doColours, Callback callback) {
        super(callback);
        this.doColours = doColours;
    }

    @Override
    public void init() {
        CodeClient.MC.player.sendCommand("dumpactioninfo");
        capturedData = new StringBuilder();
        startTime = new Date();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(capturedData == null || isDone) return false;
        if(packet instanceof GameMessageS2CPacket message) {
            String content = message.content().getString();
            capturedData.append(content);
            capturedData.append("\n");

//            capturedData = capturedData + content;

            lines += 1;
            length += content.length();
            OverlayManager.setOverlayText();
            OverlayManager.addOverlayText(Text.literal("§6GetActionDump:"));
            OverlayManager.addOverlayText(Text.literal("§bSize: §a" + length));
            OverlayManager.addOverlayText(Text.literal("§bLines: §a" + lines));
            OverlayManager.addOverlayText(Text.literal("§bTime: §a" + ((float) (new Date().getTime() - startTime.getTime()) / 1000)));
            if(Objects.equals(content, "}")) {
                isDone = true;
                OverlayManager.addOverlayText(Text.literal(""));
                OverlayManager.addOverlayText(Text.literal("§dComplete!"));
                CodeClient.LOGGER.info(capturedData.toString());
            }
            return true;
        }
        return false;
    }
}
