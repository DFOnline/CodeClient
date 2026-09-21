package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeSlot extends AbstractWidget {
    public static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/container/slot.png");
    private final AbstractContainerMenu handler;
    public boolean disabled = false;
    @NotNull
    public ItemStack item = ItemStack.EMPTY;

    public FakeSlot(int x, int y, Component message, AbstractContainerMenu handler) {
        super(x, y, 18, 18, message);
        this.handler = handler;
        this.item = Items.STRING.getDefaultInstance();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {/*TODO(1.21.8)
        graphics.drawTexture(RenderLayer::getGuiTextured, TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 18);
        graphics.drawItem(item, this.getX() + 1, this.getY() + 1);
        if (this.isMouseOver(mouseX, mouseY)) {
            if (CodeClient.MC.currentScreen instanceof GenericContainerScreen sc) {
                graphics.getMatrices().push();
                graphics.getMatrices().translate(mouseX, mouseY, 0.0F);
                // FIXMe: remnder
            }
            graphics.drawItemTooltip(CodeClient.MC.textRenderer, item, mouseX, mouseY);
        }*/
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y()) && ((!this.item.isEmpty()) || (!this.handler.getCarried().isEmpty()))) {
            this.handler.suppressRemoteUpdates();
            var swap = this.item.copy();
            this.item = this.handler.getCarried().copyWithCount(1);
            this.handler.setCarried(swap);
            this.handler.resumeRemoteUpdates();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Nullable
//    @Override TODO(1.21.8)
    public Tooltip getTooltip() {
        var data = Screen.getTooltipFromItem(CodeClient.MC, item);
        var text = Component.empty();
        boolean first = true;
        for (var line : data) {
            if (first) first = false;
            else text.append(Component.literal("\n"));
            text.append(line);
        }
        return Tooltip.create(text);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
