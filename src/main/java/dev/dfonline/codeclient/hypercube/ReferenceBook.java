package dev.dfonline.codeclient.hypercube;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.ItemData;
import dev.dfonline.codeclient.data.PublicBukkitValues;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class ReferenceBook {
    final ItemStack book;

    public ReferenceBook(ItemStack item) throws IllegalArgumentException {
        if (!validate(item)) throw new IllegalArgumentException("Given item is not a ReferenceBook!");
        this.book = item.copy();
    }

    private boolean validate(ItemStack item) {
        if (item.getItem() instanceof WrittenBookItem) {
            DFItem dfItem = DFItem.of(item);
            return !dfItem.getHypercubeStringValue("item_instance").isEmpty();
        }
        return false;
    }

    public boolean isEmpty() {
        return book.isEmpty() || book.getName().getString().contains("◆ Reference Book ◆");
    }

    public ItemStack getItem() {
        return book;
    }

    /**
     * This function exists to fix a compatibility issue with previewing items tags.
     * (e.g. the {@link dev.dfonline.codeclient.dev.overlay.ActionViewer}'s fallback tooltip)
     * @return the reference book without any tags applied to it.
     */
    public ItemStack getTaglessItem() {
        DFItem tagless = DFItem.of(book.copy());
        ItemData itemData = tagless.getItemData();
        if (itemData == null) return book;
        itemData.removeKey(PublicBukkitValues.PUBLIC_BUKKIT_VALUES_KEY);
        tagless.setItemData(itemData);
        return tagless.getItemStack();
    }

    public List<Text> getTooltip() {
        return getTaglessItem().getTooltip(Item.TooltipContext.DEFAULT, CodeClient.MC.player, TooltipType.BASIC);
    }

    // todo: parse into action?

}
