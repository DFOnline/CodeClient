package dev.dfonline.codeclient.hypercube;

import net.minecraft.component.DataComponentTypes;
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
            var customData = item.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return false;
            var nbt = customData.copyNbt();
            var publicBukkit = nbt.getCompound("PublicBukkitValues");
            return publicBukkit != null && !publicBukkit.getString("hypercube:item_instance").isEmpty();
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
        return book.getTooltip(null, null, TooltipType.BASIC);
    }

    // todo: parse into action?

}
