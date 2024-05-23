package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class Text extends NamedItem {
    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","string");
        return object;
    }

    public Text(JsonObject var) {
        super(var);
    }

    public Text() {
        super("string");
    }

    @Override
    public String getId() {
        return "txt";
    }

    @Override
    protected Item getIconItem() {
        return Items.STRING;
    }
}
