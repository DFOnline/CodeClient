package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class NamedItem extends VarItem {
    private String name;

    public NamedItem(Item material, JsonObject var) {
        super(material, var);
        this.name = this.data.get("name").getAsString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.data.addProperty("name",name);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        return stack.setCustomName(Text.literal(name).setStyle(Style.EMPTY.withItalic(false)));
    }
}
