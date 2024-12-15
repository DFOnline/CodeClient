package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public abstract class NamedItem extends VarItem {
    private String name;

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","name");
        return object;
    }

    public NamedItem(JsonObject var) {
        super(var);
        this.name = this.data.get("name").getAsString();
    }

    public NamedItem() {
        this("name");
    }

    public NamedItem(String name) {
        super();
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.data.addProperty("name", name);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Text.literal(name).setStyle(Style.EMPTY.withItalic(false)));
        return dfItem.getItemStack();
    }
}
