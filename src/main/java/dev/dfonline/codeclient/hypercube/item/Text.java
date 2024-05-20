package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

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
        super();
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    protected Item getIconItem() {
        return null;
    }
}
