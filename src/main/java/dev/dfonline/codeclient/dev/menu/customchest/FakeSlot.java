package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeSlot extends ClickableWidget {
    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/sprites/container/slot.png");
    private final ScreenHandler handler;
    public boolean disabled = false;
    @NotNull
    public ItemStack item = ItemStack.EMPTY;

    public FakeSlot(int x, int y, Text message, ScreenHandler handler) {
        super(x, y, 18, 18, message);
        this.handler = handler;
        this.item = Items.STRING.getDefaultStack();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 18);
        context.drawItem(item, this.getX() + 1, this.getY() + 1);
        if (this.isMouseOver(mouseX, mouseY)) {
            if (CodeClient.MC.currentScreen instanceof GenericContainerScreen sc) {
                context.getMatrices().push();
                context.getMatrices().translate(mouseX, mouseY, 0.0F);
                // FIXMe: remnder
            }
            context.drawItemTooltip(CodeClient.MC.textRenderer, item, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY) && ((!this.item.isEmpty()) || (!this.handler.getCursorStack().isEmpty()))) {
            this.handler.disableSyncing();
            var swap = this.item.copy();
            this.item = this.handler.getCursorStack().copyWithCount(1);
            this.handler.setCursorStack(swap);
            this.handler.enableSyncing();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public Tooltip getTooltip() {
        var data = Screen.getTooltipFromItem(CodeClient.MC, item);
        var text = Text.empty();
        boolean first = true;
        for (var line : data) {
            if (first) first = false;
            else text.append(Text.literal("\n"));
            text.append(line);
        }
        return Tooltip.of(text);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
