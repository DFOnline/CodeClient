package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class Component extends NamedItem {
    public Component(JsonObject var) {
        super(var);
    }

    public Component() {
        super("Text");
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","Text");
        return super.getDefaultData();
    }

    @Override
    public String getId() {
        return "comp";
    }

    @Override
    protected Item getIconItem() {
        return Items.BOOK;
    }
}
