package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.Number;
import dev.dfonline.codeclient.hypercube.item.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.hypercube.item.Variable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public class ValueDetails extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().ValueDetails;
    }

    public void draw(DrawText drawText, Font textRenderer, ItemStack stack, int x, int y, Matrix3x2fStack matrices) {
        var varItem = VarItems.parse(stack);

        if (varItem == null) return;

        Component text = null;
        if (varItem instanceof Number number) {
            String name = number.getName();
            if (textRenderer.width(Component.nullToEmpty(name)) > 16) {
                var avail = textRenderer.plainSubstrByWidth(name, 16 - 2);
                text = Component.literal(avail).withStyle(ChatFormatting.RED).append(Component.literal(".".repeat((16 - textRenderer.width(Component.nullToEmpty(avail))) / 2)).withStyle(ChatFormatting.WHITE));
            } else text = Component.literal(name).withStyle(ChatFormatting.RED);
        }
        if(varItem instanceof Variable variable) {
            var scope = variable.getScope();
            text = Component.literal(scope.getShortName()).withColor(scope.color.getValue());
        }
        if(varItem instanceof Sound sound) {
            var note = sound.getNote();
            if(note != null) text = Component.literal(note);
        }

        if (text == null) return;

        matrices.translate(0.0F, 0.0F);
        drawText.run(textRenderer, text, x, y, 0xFF_FFFFFF, true);
        matrices.translate(0.0F, 0.0F);
    }

    public interface DrawText {
        void run(Font textRenderer, Component text, int x, int y, int color, boolean shadow);
    }
}
