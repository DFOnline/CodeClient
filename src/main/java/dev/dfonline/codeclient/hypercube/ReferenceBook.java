package dev.dfonline.codeclient.hypercube;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WrittenBookItem;
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
            var nbt = item.getSubNbt("PublicBukkitValues");
            return nbt != null && !nbt.getString("hypercube:item_instance").isEmpty();
        }
        return false;
    }

    public boolean isEmpty() {
        return book.isEmpty() || book.getName().getString().contains("◆ Reference Book ◆");
    }

    public ItemStack getItem() {
        return book;
    }

    public List<Text> getTooltip() {
        return book.getTooltip(null, TooltipContext.BASIC);
    }

    // todo: parse into action?

}
