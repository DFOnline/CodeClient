package dev.dfonline.codeclient.switcher;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A switcher screen which looks like the F3+F4 game mode switcher.
 * It can reasonably hold up to 4 options.
 */
public abstract class GenericSwitcher extends Screen {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/gamemode_switcher.png");
    private static final Identifier SLOT_TEXTURE = Identifier.withDefaultNamespace("gamemode_switcher/slot");
    private static final Identifier SELECTED_TEXTURE = Identifier.withDefaultNamespace("gamemode_switcher/selection");
    /**
     * Key to hold down, generally F3.
     * The selected option will be run when this is released.
     */
    public final int HOLD_KEY;
    /**
     * The key to open and select the next option.
     */
    public final int PRESS_KEY;
    private final List<SelectableButtonWidget> buttons = new ArrayList<>();
    protected boolean hasClicked = false;
    protected Integer selected;
    protected Component footer = Component.translatable("codeclient.switcher.footer");
    private boolean usingMouseToSelect = false;
    private Integer lastMouseX;
    private Integer lastMouseY;

    protected GenericSwitcher(Component title, int holdKey, int pressKey) {
        super(title);
        HOLD_KEY = holdKey;
        PRESS_KEY = pressKey;
    }


    abstract List<Option> getOptions();

    @Override
    protected void init() {
        super.init();
        this.usingMouseToSelect = false;
        List<Option> options = getOptions();
        int width = options.size() * 31 - 5;
        int i = 0;
        for (Option option : options) {
            this.buttons.add(new SelectableButtonWidget(option, this.width / 2 - width / 2 + i * 31, this.height / 2 - 31));
            ++i;
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.buttons.clear();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (checkFinished()) return;
        int centerX = this.width / 2 - 62;
        int centerY = this.height / 2 - 31 - 27;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, centerX, centerY, 0, 0, 125, 75, 128, 128);
        super.render(context, mouseX, mouseY, delta);

        if (lastMouseX == null) lastMouseX = mouseX;
        if (lastMouseY == null) lastMouseY = mouseY;

        if (!usingMouseToSelect) {
            if (this.lastMouseX != mouseX || this.lastMouseY != mouseY) this.usingMouseToSelect = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        Option selected = getSelected();
        Component selectedText = selected != null ? selected.text : Component.translatable("codeclient.switcher.select");

        context.drawCenteredString(this.font, selectedText, this.width / 2, this.height / 2 - 51, CommonColors.WHITE);
        context.drawCenteredString(this.font, footer, this.width / 2, this.height / 2 + 5, CommonColors.WHITE);

        int i = 0;

        for (SelectableButtonWidget button : buttons) {
            if (usingMouseToSelect) {
                if (button.getX() < mouseX && button.getX() + 31 > mouseX) this.selected = i;
            }
            button.selected = this.selected == i;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, button.getX(), button.getY(), 26, 26);
            if (button.selected)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTED_TEXTURE, button.getX(), button.getY(), 26, 26);
            button.render(context, mouseX, mouseY, delta);
            ++i;
        }
    }

    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean keyPressed(KeyEvent key) {
        if (key.input() == PRESS_KEY) {
            this.usingMouseToSelect = false;
            this.selected++;
            this.selected %= getOptions().size();
            return true;
        }
        if (key.input() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
        }
        return super.keyPressed(key);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int i = 0;
        for (SelectableButtonWidget widget : buttons) {
            if (widget.getX() < click.x() && widget.getX() + 31 > click.x()
                    && widget.getY() < click.y() && widget.getY() + 31 > click.y()) this.selected = i;
            widget.selected = this.selected == i;
            if (widget.selected) {
                hasClicked = true;
                return true;
            }
            ++i;
        }
        return super.mouseClicked(click, doubled);
    }

    protected boolean checkFinished() {
        if (this.minecraft == null) return false;
        if (hasClicked || !InputConstants.isKeyDown(this.minecraft.getWindow(), HOLD_KEY)) {
            Option selected = getSelected();
            if (selected != null) selected.run();
            this.minecraft.setScreen(null);
            return true;
        }
        return false;
    }

    protected Option getSelected() {
        List<Option> options = getOptions();
        if (selected >= options.size()) return null;
        if (selected < 0) return null;
        return options.get(selected);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    public interface Callback {
        void run();
    }

    @Environment(EnvType.CLIENT)
    public record Option(Component text, ItemStack icon, Callback callback) {
        public void run() {
            callback.run();
        }
    }

    @Environment(EnvType.CLIENT)
    private class SelectableButtonWidget extends AbstractWidget {
        final Option option;
        public boolean selected = false;

        public SelectableButtonWidget(Option option, int x, int y) {
            super(x, y, 26, 26, option.text());
            this.option = option;
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
//            context.getMatrices().translate((float) this.getX(), (float) this.getY(), 0.0F);
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);

            context.renderItem(option.icon, this.getX() + 5, this.getY() + 5);
            context.renderItemDecorations(font, option.icon, this.getX() + 5, this.getY() + 5);

            if (selected) {
//                context.getMatrices().translate((float) this.getX(), (float) this.getY(), 0.0F);
                context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 26, 75, 26, 26, 128, 128);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            this.defaultButtonNarrationText(builder);
        }
    }
}
