package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

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
        CodeClient.MC.player.commandUnsigned("dumpactioninfo");
        capturedData = new StringBuilder();
        startTime = new Date();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(capturedData == null || isDone) return false;
        if(packet instanceof ClientboundSystemChatPacket message) {
            String content = message.content().getString();
            capturedData.append(content);
            capturedData.append("\n");
            lines += 1;
            length += content.length();
            OverlayManager.setOverlayText();
            OverlayManager.addOverlayText(Component.literal("§6GetActionDump:"));
            OverlayManager.addOverlayText(Component.literal("§bSize: §a" + length));
            OverlayManager.addOverlayText(Component.literal("§bLines: §a" + lines));
            OverlayManager.addOverlayText(Component.literal("§bTime: §a" + ((float) (new Date().getTime() - startTime.getTime()) / 1000)));
            if(Objects.equals(content, "}")) {
                isDone = true;
                OverlayManager.addOverlayText(Component.literal(""));
                OverlayManager.addOverlayText(Component.literal("§dComplete!"));
                OverlayManager.addOverlayText(Component.literal("§dType §a/abort §dto hide this."));
                OverlayManager.addOverlayText(Component.literal(""));
                try {
                    FileManager.writeFile("actiondump.json",capturedData.toString());
                    OverlayManager.addOverlayText(Component.literal("§dThere will now be a file in your Minecraft directory."));
                    OverlayManager.addOverlayText(Component.literal("§a" + FileManager.Path() + "\\actiondump.json"));
                } catch (IOException e) {
                    OverlayManager.addOverlayText(Component.literal("§cAn error occurred whilst writing to a file,"));
                    OverlayManager.addOverlayText(Component.literal("§cso instead it has been written to your console."));
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
