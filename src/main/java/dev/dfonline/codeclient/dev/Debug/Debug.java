package dev.dfonline.codeclient.dev.Debug;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
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
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Debug {
    private static Double CPU = null;
    private static List<Text> text = null;
    private static HashMap<Vec3d, Text> locations = new HashMap<>();
    private static boolean active = false;

    public static void clean() {
        CPU = null;
        text = null;
        locations = new HashMap<>();
        active = false;
    }

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if (CodeClient.location instanceof Plot plot) {
            if (!Config.getConfig().CCDBUG) return false;
            if (packet instanceof OverlayMessageS2CPacket overlay) {
                var txt = overlay.getMessage();
                if (Text.empty().equals(txt)) {
                    return false;
                }
                var siblings = txt.getSiblings();
                if (siblings.isEmpty()) {
                    return false;
                }
                var command = siblings.get(0);
                if (!Objects.equals(command.getStyle().getColor(), TextColor.fromRgb(0x00ccdb))) return false;
                if (command.getContent().equals(new PlainTextContent.Literal("text"))) {
                    boolean first = true;
                    text = new ArrayList<>();
                    for (var part : siblings) {
                        if (first) {
                            first = false;
                            continue;
                        }
                        text.add(part);
                    }
                }
                if (command.getContent().equals(new PlainTextContent.Literal("location"))) {
                    var locList = new HashMap<Vec3d, Text>();
                    boolean first = true;
                    for (var part : siblings) {
                        if (first) {
                            first = false;
                            continue;
                        }
                        if (part.getSiblings().size() < 2) continue;
                        var pos = Pattern.compile("-?\\d+(.\\d+)?")
                                .matcher(part.getSiblings().get(0).getString())
                                .results()
                                .map(MatchResult::group)
                                .toList();
                        if (pos.size() < 3) continue;
                        try {
                            var vec = new Vec3d(Double.parseDouble(pos.get(0)), Double.parseDouble(pos.get(1)), Double.parseDouble(pos.get(2))).add(plot.getPos());
                            locList.put(vec, part.getSiblings().get(1));
                        } catch (Exception ignored) {
                        }
                    }
                    locations = locList;
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
        }
        return false;
    }

    public static void updateDisplay() {
        if (!active) return;
        if (text == null) return;
        OverlayManager.setOverlayText();
        OverlayManager.addOverlayText(Text.empty().append(Text.literal("CCDBUG").formatted(Formatting.YELLOW, Formatting.BOLD)).append(" (/abort to hide)"));
        if (CPU != null) {
            OverlayManager.addOverlayText(Text.literal("CPU Usage: ").formatted(Formatting.GOLD).append(Text.literal(String.valueOf(CPU)).formatted(Formatting.AQUA)));
            OverlayManager.addOverlayText(Text.empty());
        }
        for (var line : text) {
            OverlayManager.addOverlayText(line);
        }
    }

    public static void tick() {
        if (Config.getConfig().CCDBUG && active && CodeClient.location instanceof Plot) {
            updateDisplay();
        } else {
            CPU = null;
        }
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
        if (active) for (var entry : locations.entrySet())
            DebugRenderer.drawString(matrices, vertexConsumers, entry.getValue().getString(), entry.getKey().x, entry.getKey().y, entry.getKey().z, 0xFFFFFF, 0.02F, true, 0, true);
    }
}
