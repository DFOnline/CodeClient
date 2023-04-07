package dev.dfonline.codeclient.dev.Debug;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Debug {
    public static boolean active = false;

    public static Variables variables = new Variables();
    public static Double CPU = null;
    private static Variable variable;

    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if(packet instanceof OverlayMessageS2CPacket overlay) {
            String message = overlay.getMessage().getString();
            String[] args = message.split(" ");
            if(args.length > 0 && args[0].equals("ccdbug")) {
                if(args.length > 1) {
                    if(args[1].equals("hello")) {
                        active = true;
                        if(args.length > 3 && CodeClient.location instanceof Plot plot) {
                            plot.setOrigin(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                        }
                    }
                    if(args.length > 2 && args[1].equals("var")) {
                        if(args[2].equals("set")) {
                            variable = new Variable(message.replaceFirst("^ccdbug var set ",""));
                        }
                        if(args[2].equals("type")) {
                            Variable.ValueType type = Variable.ValueType.valueTypeMap.get(message.replaceFirst("^ccdbug var type ",""));
                            variable.type = type;
                            if(type == Variable.ValueType.Dead) {
                                variables.addOrUpdate(variable);
                                variable = null;
                            }
                        }
                        if(args[2].equals("value")) {
                            variable.value = message.replaceFirst("^ccdbug var value ", "");
                            variables.addOrUpdate(variable);
                            variable = null;
                        }
                    }
                }
//                updateDisplay();
                return true;
            }
            if(message.matches("^CPU Usage: \\[▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮] \\([\\d\\.]+%\\)$")) {
                CPU = Double.parseDouble(message.replaceAll("(^CPU Usage: \\[▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮] \\(|%\\)$)",""));
                return true;
            }
        }
        return false;
    }

    public static void updateDisplay() {
        OverlayManager.setOverlayText();
        OverlayManager.addOverlayText(Text.literal("CCDBUG").formatted(Formatting.YELLOW,Formatting.BOLD));
        if(CPU != null) {
            OverlayManager.addOverlayText(Text.literal("CPU Usage: ").formatted(Formatting.GOLD).append(Text.literal(String.valueOf(CPU)).formatted(Formatting.AQUA)));
        }
        OverlayManager.addOverlayText(Text.empty());
        List<Variable> variableList = List.copyOf(variables.variables);
        for (Variable variable: variableList) {
            MutableText text = Text.literal(variable.type.name).fillStyle(Style.EMPTY.withColor(variable.type.color)).append(" ").append(Text.literal(variable.name).formatted(Formatting.YELLOW));
            if(variable.value != null) {
                text.append(" ").append(Text.literal(variable.value).formatted(Formatting.AQUA));
            }
            OverlayManager.addOverlayText(text);
        }
    }

    public static void tick() {
        if(active && CodeClient.location instanceof Plot) {
            updateDisplay();
        }
        else {
            active = false;
            variables.clear();
            CPU = null;
        }
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
        for (Variable variable: variables.variables) {
            if(variable.type == Variable.ValueType.Loc && CodeClient.location instanceof Plot plot) {
                try {
                    String[] posmaker = variable.value.replaceAll("^\\[|,|]$","").split(" ");
                    Vec3d pos = new Vec3d(
                            Double.parseDouble(posmaker[0]),
                            Double.parseDouble(posmaker[1]),
                            Double.parseDouble(posmaker[2]))
                        .add(plot.getX(),0, plot.getZ());
                    DebugRenderer.drawString(matrices,vertexConsumers, variable.name, pos.x,pos.y,pos.z, 0xFFFFFF, 0.02F, true, 0, true);
                } catch (Exception ignored) {}
            }
        }
    }
}
