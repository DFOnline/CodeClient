package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.Number;
import dev.dfonline.codeclient.hypercube.item.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.hypercube.item.Variable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

public class ValueDetails extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().ValueDetails;
    }

    public void draw(DrawText drawText, TextRenderer textRenderer, ItemStack stack, int x, int y, Matrix3x2fStack matrices) {
        var varItem = VarItems.parse(stack);

        if (varItem == null) return;

        Text text = null;
        if (varItem instanceof Number number) {
            String name = number.getName();
            if (textRenderer.getWidth(Text.of(name)) > 16) {
                var avail = textRenderer.trimToWidth(name, 16 - 2);
                text = Text.literal(avail).formatted(Formatting.RED).append(Text.literal(".".repeat((16 - textRenderer.getWidth(Text.of(avail))) / 2)).formatted(Formatting.WHITE));
            } else text = Text.literal(name).formatted(Formatting.RED);
        }
        if(varItem instanceof Variable variable) {
            var scope = variable.getScope();
            text = Text.literal(scope.getShortName()).withColor(scope.color.getRgb());
        }
        if(varItem instanceof Sound sound) {
            var note = sound.getNote();
            if(note != null) text = Text.literal(note);
        }

        if (text == null) return;

        matrices.translate(0.0F, 0.0F);
        drawText.run(textRenderer, text, x, y, 0xFF_FFFFFF, true);
        matrices.translate(0.0F, 0.0F);
    }

    public interface DrawText {
        void run(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow);
    }
}
