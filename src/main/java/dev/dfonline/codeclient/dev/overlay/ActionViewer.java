package dev.dfonline.codeclient.dev.overlay;

import com.google.common.collect.Lists;
import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.ReferenceBook;
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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.List;

public class ActionViewer extends Feature {
    private Action action = null;
    private ReferenceBook book = null;
    private boolean tall = false;

    @Override
    public void reset() {
        action = null;
        book = null;
        tall = false;
    }
    public boolean isValid() {
        return action != null || book != null;
    }

    public void onClickChest(BlockHitResult hitResult) {
        reset();
        var world = CodeClient.MC.world;
        if (world == null || !Config.getConfig().ActionViewer) return;
        var position = hitResult.getBlockPos();
        if (CodeClient.location instanceof Dev dev && dev.isInDev(position) && world.getBlockEntity(position) instanceof ChestBlockEntity) {
            var signEntity = world.getBlockEntity(position.down().west());
            if (signEntity instanceof SignBlockEntity sign) {
                try {
                    action = ActionDump.getActionDump().findAction(sign.getFrontText());
                } catch (Exception ignored) {

                }
            }
        }
    }

    @Override
    public @Nullable ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new Overlay(screen);
    }

    private class Overlay extends ChestFeature {
        private boolean hover = false;
        private int scroll = 0;
        ActionTooltipPositioner positioner = new ActionTooltipPositioner();

        public Overlay(HandledScreen<?> screen) {
            super(screen);
        }

        public List<Text> getOverlayText() {
            if (CodeClient.location instanceof Dev dev) {
                if (!Config.getConfig().ActionViewer) return null;
                if (book != null) return book.getTooltip();
                if (action == null) {
                    var referenceBook = dev.getReferenceBook();
                    if (referenceBook == null || referenceBook.isEmpty()) return null;
                    book = referenceBook;
                    return referenceBook.getTooltip();
                }
                var item = action.icon.getItem();
                return item.getTooltip(Item.TooltipContext.DEFAULT, CodeClient.MC.player, TooltipType.BASIC);
            }
            return null;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, int x, int y, float delta) {
            var text = getOverlayText();
            if (text == null) return;

            positioner.setMousePosition(mouseX, mouseY); // I would pass these through like normal, but draw tooltip might be moved to a utility class in the future.

            var z = hover ? 500 : 300;
            drawTooltip(context, screen, transformText(text), 176, scroll, z);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!(screen instanceof GenericContainerScreen || screen instanceof CustomChestMenu)) return false;
            if (isValid() && tall && hover) {
                var modifier = Config.getConfig().InvertActionViewerScroll ? -8 : 8;

                scroll += modifier * (int) verticalAmount;
                return true;
            }
            return false;
        }

        private List<TooltipComponent> transformText(List<Text> texts) {
            var orderedText = Lists.transform(texts, Text::asOrderedText);
            return orderedText.stream().map(TooltipComponent::of).toList();
        }

        // this implementation of draw tooltip allows a change in the z-index of the rendered tooltip.
        private void drawTooltip(DrawContext context, HandledScreen<?> handledScreen, List<TooltipComponent> components, int x, int y, int z) {
            var textRenderer = CodeClient.MC.textRenderer;

            if (handledScreen instanceof CustomChestMenu menu) {
                var handler = menu.getScreenHandler();
                positioner.setBackgroundHeight(handler.numbers.MENU_HEIGHT);
                positioner.setBackgroundWidth(handler.numbers.MENU_WIDTH);
                x += handler.numbers.MENU_WIDTH - 176;
            } else {
                positioner.setBackgroundHeight(166);
                positioner.setBackgroundWidth(176);
            }

            if (components.isEmpty()) return;
            int tooltipWidth = 0;
            int tooltipHeight = components.size() == 1 ? -2 : 0;

            TooltipComponent tooltipComponent;
            for (Iterator<TooltipComponent> iterator = components.iterator(); iterator.hasNext(); tooltipHeight += tooltipComponent.getHeight(textRenderer)) {
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
            context.draw((vertexConsumer) -> TooltipBackgroundRenderer.render(context, vector.x(), vector.y(), finalWidth, finalHeight, z, null));
            context.getMatrices().translate(0.0F, 0.0F, (float) z);

            int textY = vector.y();
            for (int index = 0; index < components.size(); ++index) {
                tooltipComponent = components.get(index);
                TooltipComponent finalTooltipComponent = tooltipComponent;
                int finalTextY = textY;
                context.draw(consumer -> finalTooltipComponent.drawText(textRenderer, vector.x(), finalTextY, context.getMatrices().peek().getPositionMatrix(), (VertexConsumerProvider.Immediate) consumer));
                textY += tooltipComponent.getHeight(textRenderer) + (index == 0 ? 2 : 0);
            }

            context.getMatrices().pop();
        }

        private class ActionTooltipPositioner implements TooltipPositioner {
            private int backgroundHeight = 166;
            private int backgroundWidth = 176;
            private int mouseX = 0, mouseY = 0;

            private ActionTooltipPositioner() {
            }

            public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
                var vector = new Vector2i(x, y);
                var centered = Config.getConfig().ActionViewerLocation == Config.ActionViewerAlignment.CENTER;

                // can scroll or not.
                var minHeight = Math.min(backgroundHeight, screenHeight - 8);
                if (minHeight < height + 6) {
                    if (!tall) tall = true;

                    var max = ((height + 8) - minHeight) / 2;
                    var actual_scroll = centered ? Math.max(-max, Math.min(max, scroll)) : Math.max(max * -2, Math.min(0, scroll));
                    if (actual_scroll != scroll) {
                        var change = actual_scroll - scroll;
                        vector.add(0, change);
                        scroll = actual_scroll;
                    }
                }

                var difference = centered ? (height - backgroundHeight) / 2 : -4;
                vector.add(6, -difference); // alignment change

                if (backgroundHeight > screenHeight) {
                    vector.add(0, (backgroundHeight - (screenHeight - 8)) / 2);
                }

                int space = ((screenWidth - backgroundWidth) / 2) - 6 /* for padding */;
                hover = isMouseInside(x + 6, y - difference, width, height, screenWidth, screenHeight);
                if (width + 6 > space && hover) {
                    vector.add(space - width - 5, 0);
                }

                return vector;
            }

            private boolean isMouseInside(int x, int y, int width, int height, int screenWidth, int screenHeight) {
                // positioning changes
                x += (screenWidth - backgroundWidth) / 2;
                y += (screenHeight - backgroundHeight) / 2;

                // tooltip padding changes
                x -= 4;
                y -= 5;
                width += 6;
                height += 6;

                // check in range
                if (x <= mouseX && mouseX <= x + width) {
                    return y <= mouseY && mouseY <= y + height;
                }
                return false;
            }

            public void setBackgroundHeight(int backgroundHeight) {
                this.backgroundHeight = backgroundHeight;
            }

            public void setBackgroundWidth(int backgroundWidth) {
                this.backgroundWidth = backgroundWidth;
            }

            public void setMousePosition(int x, int y) {
                mouseX = x;
                mouseY = y;
            }
        }
    }
}
