package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
        this.data.addProperty("name",name);
        this.name = name;
    }
}
