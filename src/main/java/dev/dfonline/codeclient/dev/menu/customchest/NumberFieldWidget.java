package dev.dfonline.codeclient.dev.menu.customchest;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

public class NumberFieldWidget extends TextFieldWidget {
    @Nullable
    public Double min = null;
    @Nullable
    public Double max = null;
    private double number = 0;
    private @RegExp String regex = "(?<!^)-|[^\\d-.]";
    private boolean isInt = false;

    public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public NumberFieldWidget integer() {
        this.regex = "(?<!^)-|[^\\d-]";
        this.isInt = true;
        return this;
    }

    public NumberFieldWidget min(double min) {
        this.min = min;
        return this;
    }

    public NumberFieldWidget max(double max) {
        this.max = max;
        return this;
    }

    private void setValue(double number) {
        if (min != null) {
            number = Math.max(min, number);
        }
        if (max != null) {
            number = Math.min(max, number);
        }
        this.number = number;
    }

    @Override
    public void setText(String text) {
        try {
            setValue(Double.parseDouble(text));
        } catch (Exception ignored) {
        }
        super.setText(text);
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        setValue(number);
        if(isInt) this.setText("%.0f".formatted(this.number));
        else this.setText("%s".formatted(this.number));
    }

    public int getInt() {
        return (int) number;
    }

    @Override
    public void write(String text) {
        super.write(text.replaceAll(regex, ""));
        try {
            setValue(Double.parseDouble(this.getText()));
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            this.setNumber(this.getNumber() + (verticalAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
