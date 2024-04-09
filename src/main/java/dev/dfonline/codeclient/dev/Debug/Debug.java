package dev.dfonline.codeclient.dev.Debug;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Debug {
    private static Double CPU = null;
    private static List<Text> text = null;
    private static boolean active = false;

    public static void clean() {
        CPU = null;
        text = null;
        active = false;
    }

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if(!Config.getConfig().CCDBUG) return false;
        if(packet instanceof OverlayMessageS2CPacket overlay) {
            var txt = overlay.getMessage();
            if(Text.empty().equals(txt)) {
                Utility.debug("empty");
                return false;
            }
            var siblings = txt.getSiblings();
            if(siblings.isEmpty()) {
                Utility.debug("no siblings");
                return false;
            }
            var command = siblings.get(0);
            if(!Objects.equals(command.getStyle().getColor(), TextColor.fromRgb(0x00ccdb))) return false;
            if(command.getContent().equals(new PlainTextContent.Literal("ccdbug text"))) {
                boolean first = true;
                text = new ArrayList<>();
                for (var part: siblings) {
                    if(first) {
                        first = false;
                        continue;
                    }
                    text.add(part);
                }
            }
            active = true;
            return true;
//            if(command) {
//                if(siblings.size() > 1) {
//                    text = siblings.get(1);
//                }
//                else {
//                    text = Text.empty();
//                }
//                updateDisplay();
//            }
        }
        return false;
    }

    public static void updateDisplay() {
        if(!active) return;
        if(text == null) return;
        OverlayManager.setOverlayText();
        OverlayManager.addOverlayText(Text.empty().append(Text.literal("CCDBUG").formatted(Formatting.YELLOW,Formatting.BOLD)).append(" (/abort to hide)"));
        if(CPU != null) {
            OverlayManager.addOverlayText(Text.literal("CPU Usage: ").formatted(Formatting.GOLD).append(Text.literal(String.valueOf(CPU)).formatted(Formatting.AQUA)));
            OverlayManager.addOverlayText(Text.empty());
        }
        for(var line: text) {
            OverlayManager.addOverlayText(line);
        }
    }

    public static void tick() {
        if(Config.getConfig().CCDBUG && active && CodeClient.location instanceof Plot) {
            updateDisplay();
        }
        else {
            CPU = null;
        }
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
        if(active) DebugRenderer.drawString(matrices,vertexConsumers, "womp womp", CodeClient.MC.player.getX(),CodeClient.MC.player.getY(),CodeClient.MC.player.getZ(), 0, 0.02F, true, 0, true);
    }
}
