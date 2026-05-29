package dev.dfonline.codeclient;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
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
        if (CodeClient.MC.player != null && CodeClient.MC.player.getEntityWorld() != null) {
            ItemGroups.updateDisplayContext(FeatureFlags.DEFAULT_ENABLED_FEATURES, true, CodeClient.MC.player.getEntityWorld().getRegistryManager());
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
                context.drawItemTooltip(textRenderer, item.item, mouseX, mouseY);
            }
            i++;
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
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
    public boolean keyPressed(KeyInput input) {
        // TODO: make enter and/or number keys select a given item
        if (search.keyPressed(input)) {
            search();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        search();
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (search.charTyped(input)) {
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
