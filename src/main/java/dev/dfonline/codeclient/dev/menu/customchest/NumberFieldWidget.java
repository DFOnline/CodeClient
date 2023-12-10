package dev.dfonline.codeclient.dev.menu.customchest;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.intellij.lang.annotations.RegExp;

public class NumberFieldWidget extends TextFieldWidget {
    private double number = 0;
    private static final @RegExp String regex = "(?<!^)-|[^\\d-.]";

    public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public void setText(String text) {
        try {
            number = Double.parseDouble(text);
        }
        catch (Exception ignored) {}
        super.setText(text);
    }

    public void setNumber(double number) {
        this.setText("%.2f".formatted(number));
        this.number = number;
    }

    public double getNumber() {
        return number;
    }

    @Override
    public void write(String text) {
        super.write(text.replaceAll(regex,""));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(isMouseOver(mouseX,mouseY)) {
            this.setNumber(this.getNumber() + (verticalAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
