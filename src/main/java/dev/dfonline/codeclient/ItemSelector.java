package dev.dfonline.codeclient;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ItemSelector extends ClickableWidget {
    private final TextFieldWidget search;
    private final ArrayList<Search> items = new ArrayList<>();
    private final int itemsWidth;
    private final int screenX;
    private final int screenY;
    private final TextRenderer textRenderer;
    private final Consumer<ItemStack> consumer;
    private final int maxItems;

    private static final int searchHeight = 16;
    private static final int searchItemGap = 2;
    private static final int itemSize = 16;

    public ItemSelector(TextRenderer textRenderer, int x, int y, int width, int height, int screenX, int screenY, Consumer<ItemStack> consumer) {
        super(x, y, width, height, Text.literal("Select a message"));
        this.textRenderer = textRenderer;
        this.screenX = screenX;
        this.screenY = screenY;
        this.consumer = consumer;
        search = new TextFieldWidget(textRenderer, x, y, width, searchHeight, Text.translatable("itemGroup.search"));
        search.setPlaceholder(Text.translatable("itemGroup.search"));
        search.setFocused(true);
        if (CodeClient.MC.player != null && CodeClient.MC.player.getWorld() != null) {
            ItemGroups.updateDisplayContext(FeatureFlags.DEFAULT_ENABLED_FEATURES, true, CodeClient.MC.player.getWorld().getRegistryManager());
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
        for (ItemStack item : ItemGroups.getSearchGroup().getSearchTabStacks()) {
            if ((item.getName().getString().toLowerCase().contains(search.getText().toLowerCase().trim()))) {
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        search.renderWidget(context, mouseX, mouseY, delta);
        int i = 0;
        while (i < maxItems && i != items.size()) {
            var item = items.get(i);
            context.drawItem(item.item,
                    this.getX() + item.x,
                    this.getY() + item.y
            );
            if (
                    mouseX - this.getX() - screenX > item.x &&
                            mouseX - this.getX() - screenX < item.x + itemSize &&
                            mouseY - this.getY() - screenY > item.y &&
                            mouseY - this.getY() - screenY < item.y + itemSize
            ) {
                context.drawItemTooltip(textRenderer, item.item, mouseX - screenX, mouseY - screenY);
            }
            i++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (search.mouseClicked(mouseX, mouseY, button)) return true;
        for (Search item : items)
            if (mouseX - this.getX() > item.x &&
                    mouseX - this.getX() < item.x + itemSize &&
                    mouseY - this.getY() > item.y &&
                    mouseY - this.getY() < item.y + itemSize) {
                consumer.accept(item.item);
                return true;
            }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search.keyPressed(keyCode, scanCode, modifiers)) {
            search();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        search();
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (search.charTyped(chr, modifiers)) {
            search();
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    private record Search(ItemStack item, int x, int y) {
    }

}
