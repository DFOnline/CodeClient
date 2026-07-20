package dev.dfonline.codeclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ChestFeature {
    protected AbstractContainerScreen<?> screen;

    public ChestFeature(AbstractContainerScreen<?> screen) {
        this.screen = screen;
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, int x, int y, float delta) {}

    public void drawSlot(GuiGraphics context, Slot slot) {}

    public ItemStack getHoverStack(Slot instance) {
        return null;
    }

    public boolean mouseClicked(MouseButtonEvent click) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    public boolean keyPressed(KeyEvent key) {
        return false;
    }

    public boolean keyReleased(KeyEvent key) {
        return false;
    }

    public boolean charTyped(CharacterEvent charInput) {
        return false;
    }

    public boolean clickSlot(Slot slot, int button, ContainerInput containerInput, int syncId, int revision) {
        return false;
    }

}
