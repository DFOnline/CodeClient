package dev.dfonline.codeclient.dev.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Plot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.world.phys.Vec3;

public class Debug extends Feature {
    private Double CPU = null;
    private List<Component> text = null;
    private HashMap<Vec3, Component> locations = new HashMap<>();
    private boolean active = false;

    @Override
    public void reset() {
        CPU = null;
        text = null;
        locations = new HashMap<>();
        active = false;
    }

    public boolean onReceivePacket(Packet<?> packet) {
        if (CodeClient.location instanceof Plot plot) {
            if (!Config.getConfig().CCDBUG) return false;
            if (packet instanceof ClientboundSetActionBarTextPacket overlay) {
                var txt = overlay.text();
                if (Component.empty().equals(txt)) {
                    return false;
                }
                var siblings = txt.getSiblings();
                if (siblings.isEmpty()) {
                    return false;
                }
                var command = siblings.get(0);
                if (!Objects.equals(command.getStyle().getColor(), TextColor.fromRgb(0x00ccdb))) return false;
                if (command.getContents().equals(new PlainTextContents.LiteralContents("text"))) {
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
                if (command.getContents().equals(new PlainTextContents.LiteralContents("location"))) {
                    var locList = new HashMap<Vec3, Component>();
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
                            var vec = new Vec3(Double.parseDouble(pos.get(0)), Double.parseDouble(pos.get(1)), Double.parseDouble(pos.get(2))).add(plot.getPos());
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

    public void updateDisplay() {
        if (!active) return;
        if (text == null) return;
        OverlayManager.setOverlayText();
        OverlayManager.addOverlayText(Component.empty().append(Component.literal("CCDBUG").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).append(" (/abort to hide)"));
        if (CPU != null) {
            OverlayManager.addOverlayText(Component.literal("CPU Usage: ").withStyle(ChatFormatting.GOLD).append(Component.literal(String.valueOf(CPU)).withStyle(ChatFormatting.AQUA)));
            OverlayManager.addOverlayText(Component.empty());
        }
        for (var line : text) {
            OverlayManager.addOverlayText(line);
        }
    }

    public void tick() {
        if (Config.getConfig().CCDBUG && active && CodeClient.location instanceof Plot) {
            updateDisplay();
        } else {
            CPU = null;
        }
    }

    public void render(PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if (active) for (var entry : locations.entrySet())
            Gizmos.billboardText(entry.getValue().getString(), entry.getKey(), TextGizmo.Style.forColor(0xFFFFFF));
    }
}
