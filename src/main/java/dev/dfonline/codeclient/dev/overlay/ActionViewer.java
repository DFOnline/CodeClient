package dev.dfonline.codeclient.dev.overlay;

import com.google.common.collect.Lists;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.actiondump.Action;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.thread.ThreadExecutor;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.List;

public class ActionViewer {
    private static Action action;
    private static boolean reference = false;
    private static ItemStack book;

    public static void invalidate() {
        action = null;
        reference = false;
        book = null;
    }
    public static void onClickChest(BlockHitResult hitResult) {
        invalidate();
        var world = CodeClient.MC.world;
        if (world == null || !Config.getConfig().ActionViewer) return;
        var position = hitResult.getBlockPos();
        if (CodeClient.location instanceof Dev dev && dev.isInDev(position) && world.getBlockEntity(position) instanceof ChestBlockEntity) {
            var signEntity = world.getBlockEntity(position.down().west());
            if (signEntity instanceof SignBlockEntity sign) {
                if (sign.getFrontText().getMessage(0, true).contains(Text.literal("CALL FUNCTION"))) {
                    reference = true;
                    return;
                }
                try {
                    action = ActionDump.getActionDump().findAction(sign.getFrontText());
                } catch (Exception ignored) {

                }
            }
        }
    }

    public static List<Text> getOverlayText() {
        if (CodeClient.location instanceof Dev dev) {
            if (!Config.getConfig().ActionViewer) return null;
            ItemStack item;
            if (action == null) {
                if (!reference) return null;
                if (book == null) {
                    var rb = dev.getReferenceBook();
                    if (rb.isEmpty()) return null;
                    book = rb.getItem();
                }
                item = book;
            } else {
                item = action.icon.getItem();
            }
            return item.getTooltip(null, TooltipContext.BASIC);
        }
        return null;
    }

    public static void render(DrawContext context, HandledScreen<?> handledScreen) {
        if (!(handledScreen instanceof GenericContainerScreen || handledScreen instanceof CustomChestMenu))
            return;
        var text = getOverlayText();
        if (text == null) return;

        drawTooltip(context, handledScreen, transformText(text), 176, 0, 300);
    }

    private static List<TooltipComponent> transformText(List<Text> texts) {
        var orderedText = Lists.transform(texts, Text::asOrderedText);
        return  orderedText.stream().map(TooltipComponent::of).toList();
    }

    // this implementation of draw tooltip allows a change in the z-index of the rendered tooltip.
    private static void drawTooltip(DrawContext context, HandledScreen<?> handledScreen, List<TooltipComponent> components, int x, int y, int z) {
        var textRenderer = CodeClient.MC.textRenderer;

        var positioner = ActionTooltipPositioner.INSTANCE;
        if (handledScreen instanceof CustomChestMenu menu) {
            var handler = menu.getScreenHandler();
            positioner.setBackgroundHeight(handler.numbers.MENU_HEIGHT);
            x += handler.numbers.MENU_WIDTH - 176;
        } else {
            positioner.setBackgroundHeight(166);
        }

        if (components.isEmpty()) return;
        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;

        TooltipComponent tooltipComponent;
        for (Iterator<TooltipComponent> iterator = components.iterator(); iterator.hasNext(); tooltipHeight += tooltipComponent.getHeight()) {
            tooltipComponent = iterator.next();
            int width = tooltipComponent.getWidth(textRenderer);
            if (width > tooltipWidth) {
                tooltipWidth = width;
            }
        }
        Vector2ic vector = positioner.getPosition(context.getScaledWindowWidth(), context.getScaledWindowHeight(), x, y, tooltipWidth, tooltipHeight);
        context.getMatrices().push();

        var finalWidth = tooltipWidth;
        var finalHeight = tooltipHeight;
        context.draw(() -> TooltipBackgroundRenderer.render(context, vector.x(), vector.y(), finalWidth, finalHeight, z));
        context.getMatrices().translate(0.0F, 0.0F, (float) z);

        int textY = vector.y();
        for (int index = 0; index < components.size(); ++index) {
            tooltipComponent = components.get(index);
            tooltipComponent.drawText(textRenderer, vector.x(), textY, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers());
            textY += tooltipComponent.getHeight() + (index == 0 ? 2 : 0);
        }

        context.getMatrices().pop();
    }

    private static class ActionTooltipPositioner implements TooltipPositioner {
        public static final ActionTooltipPositioner INSTANCE = new ActionTooltipPositioner();
        private int backgroundHeight = 166;

        private ActionTooltipPositioner() {
        }

        public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            int difference = (height - backgroundHeight) / 2;
            return (new Vector2i(x, y)).add(6, -difference);
        }

        public void setBackgroundHeight(int backgroundHeight) {
            this.backgroundHeight = backgroundHeight;
        }
    }
}
