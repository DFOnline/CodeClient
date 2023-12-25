package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Number extends NamedItem {
    public Number(Item material, JsonObject var) {
        super(material, var);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        return stack.setCustomName(Text.literal(this.getName()).setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)));
    }
}
