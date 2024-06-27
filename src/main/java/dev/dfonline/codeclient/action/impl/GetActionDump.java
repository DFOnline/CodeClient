package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.*;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

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
        if (CodeClient.MC == null || CodeClient.MC.getNetworkHandler() == null) return;
        CodeClient.MC.getNetworkHandler().sendCommand("dumpactioninfo");
        capturedData = new StringBuilder();
        startTime = new Date();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (capturedData == null || isDone) return false;
        if (packet instanceof GameMessageS2CPacket message) {
            if (message.content().getString().startsWith("Error:")) {
                isDone = true;
                OverlayManager.setOverlayText(Text.translatable("codeclient.action.get_action_dump.error.could_not_start").formatted(Formatting.RED));
                OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.error.beta_broke").formatted(Formatting.RED));
                OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.abort", Text.literal("/abort").formatted(Formatting.GREEN)).formatted(Formatting.LIGHT_PURPLE));

                return true;
            }
            Utility.textToString(message.content(), capturedData, colorMode);
            String content = message.content().getString();
            capturedData.append("\n");
            lines += 1;
            length += content.length();
            OverlayManager.setOverlayText();
            OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.title").formatted(Formatting.GOLD));
            OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.size", Text.literal(String.valueOf(length)).formatted(Formatting.GREEN)).formatted(Formatting.LIGHT_PURPLE));
            OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.lines", Text.literal(String.valueOf(lines)).formatted(Formatting.GREEN)).formatted(Formatting.LIGHT_PURPLE));
            OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.time", Text.literal(String.valueOf((float) (new Date().getTime() - startTime.getTime()) / 1000)).formatted(Formatting.GREEN)).formatted(Formatting.LIGHT_PURPLE));
            if (Objects.equals(content, "}")) {
                isDone = true;
                OverlayManager.addOverlayText(Text.literal(""));
                OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.complete").formatted(Formatting.LIGHT_PURPLE));
                OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.abort", Text.literal("/abort").formatted(Formatting.GREEN)).formatted(Formatting.LIGHT_PURPLE));
                OverlayManager.addOverlayText(Text.literal(""));
                try {
                    Path path = FileManager.writeFile("actiondump.json", capturedData.toString());
                    OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.complete.file").formatted(Formatting.LIGHT_PURPLE));
                    OverlayManager.addOverlayText(Text.literal(path.toString()).formatted(Formatting.GREEN));
                } catch (IOException e) {
                    OverlayManager.addOverlayText(Text.translatable("codeclient.files.error.cant_save").formatted(Formatting.RED));
                    OverlayManager.addOverlayText(Text.translatable("codeclient.action.get_action_dump.scanning.complete.save_error").formatted(Formatting.RED));
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
