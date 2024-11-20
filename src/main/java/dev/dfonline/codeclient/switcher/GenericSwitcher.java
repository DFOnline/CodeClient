package dev.dfonline.codeclient.switcher;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A switcher screen which looks like the F3+F4 game mode switcher.
 * It can reasonably hold up to 4 options.
 */
public abstract class GenericSwitcher extends Screen {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/gamemode_switcher.png");
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("gamemode_switcher/slot");
    private static final Identifier SELECTED_TEXTURE = Identifier.ofVanilla("gamemode_switcher/selection");
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
    protected Text footer = Text.translatable("codeclient.switcher.footer");
    private boolean usingMouseToSelect = false;
    private Integer lastMouseX;
    private Integer lastMouseY;

    protected GenericSwitcher(Text title, int holdKey, int pressKey) {
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
    protected void clearChildren() {
        super.clearChildren();
        this.buttons.clear();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (checkFinished()) return;
        context.getMatrices().push();
        RenderSystem.enableBlend();
        int centerX = this.width / 2 - 62;
        int centerY = this.height / 2 - 31 - 27;
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, centerX, centerY, 0, 0, 125, 75, 128, 128);
        context.getMatrices().pop();
        super.render(context, mouseX, mouseY, delta);

        if (lastMouseX == null) lastMouseX = mouseX;
        if (lastMouseY == null) lastMouseY = mouseY;

        if (!usingMouseToSelect) {
            if (this.lastMouseX != mouseX || this.lastMouseY != mouseY) this.usingMouseToSelect = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        Option selected = getSelected();
        Text selectedText = selected != null ? selected.text : Text.translatable("codeclient.switcher.select");

        context.drawCenteredTextWithShadow(this.textRenderer, selectedText, this.width / 2, this.height / 2 - 51, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, footer, this.width / 2, this.height / 2 + 5, 0xFFFFFF);

        int i = 0;

        for (SelectableButtonWidget button : buttons) {
            if (usingMouseToSelect) {
                if (button.getX() < mouseX && button.getX() + 31 > mouseX) this.selected = i;
            }
            button.selected = this.selected == i;
            context.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_TEXTURE, button.getX(), button.getY(), 26, 26);
            if (button.selected)
                context.drawGuiTexture(RenderLayer::getGuiTextured, SELECTED_TEXTURE, button.getX(), button.getY(), 26, 26);
            button.render(context, mouseX, mouseY, delta);
            ++i;
        }
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == PRESS_KEY) {
            this.usingMouseToSelect = false;
            this.selected++;
            this.selected %= getOptions().size();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i = 0;

        for (SelectableButtonWidget widget : buttons) {
            if (widget.getX() < mouseX && widget.getX() + 31 > mouseX
                    && widget.getY() < mouseY && widget.getY() + 31 > mouseY) this.selected = i;
            widget.selected = this.selected == i;
            if (widget.selected) {
                hasClicked = true;
                return true;
            }
            ++i;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean checkFinished() {
        if (this.client == null) return false;
        if (hasClicked || !InputUtil.isKeyPressed(this.client.getWindow().getHandle(), HOLD_KEY)) {
            Option selected = getSelected();
            if (selected != null) selected.run();
            this.client.setScreen(null);
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
    public boolean shouldPause() {
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
    public record Option(Text text, ItemStack icon, Callback callback) {
        public void run() {
            callback.run();
        }
    }

    @Environment(EnvType.CLIENT)
    private class SelectableButtonWidget extends ClickableWidget {
        final Option option;
        public boolean selected = false;

        public SelectableButtonWidget(Option option, int x, int y) {
            super(x, y, 26, 26, option.text());
            this.option = option;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.getMatrices().push();
            context.getMatrices().translate((float) this.getX(), (float) this.getY(), 0.0F);
            context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);
            context.getMatrices().pop();

            context.drawItem(option.icon, this.getX() + 5, this.getY() + 5);
            context.drawStackOverlay(textRenderer, option.icon, this.getX() + 5, this.getY() + 5);

            if (selected) {
                context.getMatrices().push();
                context.getMatrices().translate((float) this.getX(), (float) this.getY(), 0.0F);
                context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, 26, 75, 26, 26, 128, 128);
                context.getMatrices().pop();
            }
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
