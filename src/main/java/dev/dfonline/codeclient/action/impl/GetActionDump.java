package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.io.IOException;
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
        CodeClient.MC.getNetworkHandler().sendCommand("dumpactioninfo" + (doColours ? "-c" : ""));
        capturedData = new StringBuilder();
        startTime = new Date();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(capturedData == null || isDone) return false;
        if(packet instanceof GameMessageS2CPacket message) {
            TextColor lastColor = null;
            for(Text text : message.content().getSiblings()) {
                TextColor color = text.getStyle().getColor();
                if(lastColor != color) {
                    lastColor = color;
                    if(color.getName().contains("#")) {
                        capturedData.append(String.join("§",color.getName().split("")).replace("#","§x").toLowerCase());
                    } else {
                        capturedData.append(Formatting.valueOf(String.valueOf(color).toUpperCase()));
                    }
                }
                capturedData.append(text.getString());
            }
            String content = message.content().getString();
            capturedData.append("\n");
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
                OverlayManager.addOverlayText(Text.literal("§dType §a/abort §dto hide this."));
                OverlayManager.addOverlayText(Text.literal(""));
                try {
                    FileManager.writeFile("actiondump.json",capturedData.toString());
                    OverlayManager.addOverlayText(Text.literal("§dThere will now be a file in your Minecraft directory."));
                    OverlayManager.addOverlayText(Text.literal("§a" + FileManager.Path() + "\\actiondump.json"));
                } catch (IOException e) {
                    OverlayManager.addOverlayText(Text.literal("§cAn error occurred whilst writing to a file,"));
                    OverlayManager.addOverlayText(Text.literal("§cso instead it has been written to your console."));
                }
                callback();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        return capturedData != null && !isDone;
    }
}
