package dev.dfonline.codeclient.switcher;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.dfonline.codeclient.CodeClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GenericSwitcher extends Screen {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/gamemode_switcher.png");
    protected List<Option> options = new ArrayList<>();
    private List<SelectableButtonWidget> buttons = new ArrayList<>();

    protected GenericSwitcher(Text title) {
        super(title);
    }


    @Override
    protected void init() {
        super.init();
        int width = options.size() * 31 - 5;
        int i = 0;
        for (Option option : options) {
            this.buttons.add(new SelectableButtonWidget(option,this.width / 2 - width / 2 + i * 31, this.height / 2 - 31));
            ++i;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0,TEXTURE);
        int centerX = this.width / 2 - 62;
        int centerY = this.height / 2 - 31 - 27;
        drawTexture(matrices, centerX, centerY, 0.0F, 0.0F, 125, 75, 128, 128);
        matrices.pop();
        drawCenteredTextWithShadow(matrices, this.textRenderer, Text.literal("Header"), this.width / 2, this.height / 2 - 51, 0xFFFFFF);
        drawCenteredTextWithShadow(matrices, this.textRenderer, Text.literal("Footer"), this.width / 2, this.height / 2 + 5, 0xFFFFFF);
        for (SelectableButtonWidget button : buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
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
        private boolean selected = false;

        public SelectableButtonWidget(Option option, int x, int y) {
            super(x,y,26,26,option.text());
            this.option = option;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.drawBackground(matrices, CodeClient.MC.getTextureManager());
            itemRenderer.renderInGuiWithOverrides(matrices, option.icon(), this.getX() + 5, this.getY() + 5);
        }

        private void drawBackground(MatrixStack matrices, TextureManager textureManager) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            matrices.push();
            matrices.translate((float)this.getX(), (float)this.getY(), 0.0F);
            drawTexture(matrices, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);
            matrices.pop();
        }


        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
