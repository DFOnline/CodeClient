package dev.dfonline.codeclient.switcher;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericSwitcher extends Screen {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/gamemode_switcher.png");
    private List<SelectableButtonWidget> buttons = new ArrayList<>();
    private boolean usingMouseToSelect = false;
    private Integer lastMouseX;
    private Integer lastMouseY;
    protected List<Option> options = new ArrayList<>();
    protected Integer selected;
    protected Text footer = Text.literal("No Footer");
    
    public final int HOLD_KEY;
    public final int PRESS_KEY;

    protected GenericSwitcher(Text title, int holdKey, int pressKey) {
        super(title);
        HOLD_KEY = holdKey;
        PRESS_KEY = pressKey;
    }


    @Override
    protected void init() {
        super.init();
        this.usingMouseToSelect = false;
        int width = options.size() * 31 - 5;
        int i = 0;
        for (Option option : options) {
            this.buttons.add(new SelectableButtonWidget(option,this.width / 2 - width / 2 + i * 31, this.height / 2 - 31));
            ++i;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(checkFinished()) return;
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0,TEXTURE);
        int centerX = this.width / 2 - 62;
        int centerY = this.height / 2 - 31 - 27;
        drawTexture(matrices, centerX, centerY, 0.0F, 0.0F, 125, 75, 128, 128);
        matrices.pop();

        if(lastMouseX == null) lastMouseX = mouseX;
        if(lastMouseY == null) lastMouseY = mouseY;
        if(!usingMouseToSelect) {
            if(this.lastMouseX != mouseX || this.lastMouseY != mouseY) this.usingMouseToSelect = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        Option selected = getSelected();
        Text selectedText = selected != null ? selected.text : Text.literal("Select");
        drawCenteredTextWithShadow(matrices, this.textRenderer, selectedText, this.width / 2, this.height / 2 - 51, 0xFFFFFF);
        drawCenteredTextWithShadow(matrices, this.textRenderer, footer, this.width / 2, this.height / 2 + 5, 0xFFFFFF);
        int i = 0;
        for (SelectableButtonWidget button : buttons) {
            if(usingMouseToSelect) {
                if(button.getX() < mouseX && button.getX() + 31 > mouseX) this.selected = i;
            }
            button.selected = Objects.equals(button.option, selected);
            button.render(matrices, mouseX, mouseY, delta);
            ++i;
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == PRESS_KEY) {
            this.usingMouseToSelect = false;
            this.selected ++;
            this.selected %= 3;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean checkFinished() {
        if(!InputUtil.isKeyPressed(this.client.getWindow().getHandle(), HOLD_KEY)) {
            Option selected = getSelected();
            if(selected != null) selected.run();
            this.client.setScreen(null);
            return true;
        }
        return false;
    }

    private Option getSelected() {
        if(selected >= options.size()) return null;
        if(selected < 0) return null;
        return options.get(selected);
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
            super(x,y,26,26,option.text());
            this.option = option;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            matrices.push();
            matrices.translate((float)this.getX(), (float)this.getY(), 0.0F);
            drawTexture(matrices, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);
            matrices.pop();

            itemRenderer.renderInGuiWithOverrides(matrices, option.icon(), this.getX() + 5, this.getY() + 5);

            if(selected) {
                RenderSystem.setShaderTexture(0, TEXTURE);
                matrices.push();
                matrices.translate((float)this.getX(), (float)this.getY(), 0.0F);
                drawTexture(matrices, 0, 0, 26.0F, 75.0F, 26, 26, 128, 128);
                matrices.pop();
            }
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
