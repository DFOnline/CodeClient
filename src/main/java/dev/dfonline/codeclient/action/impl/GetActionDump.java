package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.*;
import dev.dfonline.codeclient.action.Action;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class GetActionDump extends Action {
    private final ColorMode colorMode;
    public StringBuilder capturedData = null;
    private int lines;
    private int length;
    private Date startTime;
    private boolean isDone = false;

    public GetActionDump(ColorMode colorMode, Callback callback) {
        super(callback);
        this.colorMode = colorMode;
    }

    @Override
    public void init() {
        if (CodeClient.MC == null || CodeClient.MC.getConnection() == null) return;
        CodeClient.MC.getConnection().sendCommand("dumpactioninfo");
        capturedData = new StringBuilder();
        startTime = new Date();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (capturedData == null || isDone) return false;
        if (packet instanceof ClientboundSystemChatPacket message) {
            if (message.content().getString().startsWith("Error:")) {
                isDone = true;
                OverlayManager.setOverlayText(Component.translatable("codeclient.action.get_action_dump.error.could_not_start").withStyle(ChatFormatting.RED));
                OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.error.beta_broke").withStyle(ChatFormatting.RED));
                OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.abort", Component.literal("/abort").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.LIGHT_PURPLE));

                return true;
            }
            Utility.textToString(message.content(), capturedData, colorMode);
            String content = message.content().getString();
            capturedData.append("\n");
            lines += 1;
            length += content.length();
            OverlayManager.setOverlayText();
            OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.title").withStyle(ChatFormatting.GOLD));
            OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.size", Component.literal(String.valueOf(length)).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.LIGHT_PURPLE));
            OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.lines", Component.literal(String.valueOf(lines)).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.LIGHT_PURPLE));
            OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.time", Component.literal(String.valueOf((float) (new Date().getTime() - startTime.getTime()) / 1000)).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.LIGHT_PURPLE));
            if (Objects.equals(content, "}")) {
                isDone = true;
                OverlayManager.addOverlayText(Component.literal(""));
                OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.complete").withStyle(ChatFormatting.LIGHT_PURPLE));
                OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.abort", Component.literal("/abort").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.LIGHT_PURPLE));
                OverlayManager.addOverlayText(Component.literal(""));
                try {
                    Path path = FileManager.writeFile("actiondump.json", capturedData.toString());
                    OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.complete.file").withStyle(ChatFormatting.LIGHT_PURPLE));
                    OverlayManager.addOverlayText(Component.literal(path.toString()).withStyle(ChatFormatting.GREEN));
                } catch (IOException e) {
                    OverlayManager.addOverlayText(Component.translatable("codeclient.files.error.cant_save").withStyle(ChatFormatting.RED));
                    OverlayManager.addOverlayText(Component.translatable("codeclient.action.get_action_dump.scanning.complete.save_error").withStyle(ChatFormatting.RED));
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

    public enum ColorMode {
        NONE(null),
        AMPERSAND("&"),
        SECTION("§");

        public final String text;

        ColorMode(String text) {
            this.text = text;
        }
    }
}
