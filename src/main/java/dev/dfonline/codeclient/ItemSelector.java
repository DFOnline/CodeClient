package dev.dfonline.codeclient;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public class ItemSelector extends AbstractWidget {
    private final EditBox search;
    private final ArrayList<Search> items = new ArrayList<>();
    private final int itemsWidth;
    private final int screenX;
    private final int screenY;
    private final Font textRenderer;
    private final Consumer<ItemStack> consumer;
    private final int maxItems;

    private static final int searchHeight = 16;
    private static final int searchItemGap = 2;
    private static final int itemSize = 16;

    public ItemSelector(Font textRenderer, int x, int y, int width, int height, int screenX, int screenY, Consumer<ItemStack> consumer) {
        super(x, y, width, height, Component.literal("Select a message"));
        this.textRenderer = textRenderer;
        this.screenX = screenX;
        this.screenY = screenY;
        this.consumer = consumer;
        search = new EditBox(textRenderer, x, y, width, searchHeight, Component.translatable("itemGroup.search"));
        search.setHint(Component.translatable("itemGroup.search"));
        search.setFocused(true);
        if (CodeClient.MC.player != null && CodeClient.MC.player.level() != null) {
            CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, CodeClient.MC.player.level().registryAccess());
        }
        this.itemsWidth = width / itemSize;
        int itemsHeight = (height - searchHeight - searchItemGap) / itemSize;
        this.maxItems = itemsWidth * itemsHeight;
        search();
    }


    private void search() {
        int count = 0;
        items.clear();
        int x = 0;
        int y = 0;
        for (ItemStack item : CreativeModeTabs.searchTab().getSearchTabDisplayItems()) {
            if ((item.getHoverName().getString().toLowerCase().contains(search.getValue().toLowerCase().trim()))) {
                items.add(new Search(item, x * itemSize, y * itemSize + searchItemGap + searchHeight));
                if (++x >= itemsWidth) {
                    x = 0;
                    y++;
                }
                if (count++ > maxItems) break;
            }
        }
    }


    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        search.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
        int i = 0;
        while (i < maxItems && i != items.size()) {
            var item = items.get(i);
            graphics.item(item.item,
                    this.getX() + item.x,
                    this.getY() + item.y
            );
            if (
                    mouseX - this.getX() - screenX > item.x &&
                            mouseX - this.getX() - screenX < item.x + itemSize &&
                            mouseY - this.getY() - screenY > item.y &&
                            mouseY - this.getY() - screenY < item.y + itemSize
            ) {
                graphics.setTooltipForNextFrame(textRenderer, item.item, mouseX, mouseY);
            }
            i++;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (search.mouseClicked(click, doubled)) return true;
        for (Search item : items)
            if (click.x() - this.getX() > item.x &&
                    click.x() - this.getX() < item.x + itemSize &&
                    click.y() - this.getY() > item.y &&
                    click.y() - this.getY() < item.y + itemSize) {
                consumer.accept(item.item);
                return true;
            }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        // TODO: make enter and/or number keys select a given item
        if (search.keyPressed(input)) {
            search();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        search();
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (search.charTyped(input)) {
            search();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    private record Search(ItemStack item, int x, int y) {
    }

}
