package dev.dfonline.codeclient.dev.menu.customchest;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomChestMenu extends HandledScreen<CustomChestHandler> implements ScreenHandlerProvider<CustomChestHandler> {
    private static final Identifier TEXTURE = new Identifier(CodeClient.MOD_ID,"textures/gui/container/custom_chest/background.png");

    public CustomChestMenu(CustomChestHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
//        this.width = 202;
//        this.height = 229;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.getMatrices().push();
        RenderSystem.enableBlend();
        int centerX = this.width / 2 - 62;
        int centerY = this.height / 2 - 31 - 27;
        context.drawTexture(TEXTURE, centerX, centerY, 0.0F, 0.0F, 202, 229, 202, 229);
        context.getMatrices().pop();
    }
}
