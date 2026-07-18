package dev.dfonline.codeclient.dev.menu.customchest;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class NumberFieldWidget extends EditBox {
    @Nullable
    public Double min = null;
    @Nullable
    public Double max = null;
    private double number = 0;
    private @RegExp String regex = "(?<!^)-|[^\\d-.]";
    private boolean isInt = false;

    public NumberFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if(input.input() == GLFW.GLFW_KEY_UP) setNumber(getNumber() + 1);
        if(input.input() == GLFW.GLFW_KEY_DOWN) setNumber(getNumber() - 1);
        return super.keyPressed(input);
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
    public void setValue(String text) {
        try {
            setValue(Double.parseDouble(text));
        } catch (Exception ignored) {
        }
        super.setValue(text);
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        setValue(number);
        if(isInt) this.setValue("%.0f".formatted(this.number));
        else this.setValue("%s".formatted(this.number));
    }

    public int getInt() {
        return (int) number;
    }

    @Override
    public void insertText(String text) {
        super.insertText(text.replaceAll(regex, ""));
        try {
            setValue(Double.parseDouble(this.getValue()));
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
