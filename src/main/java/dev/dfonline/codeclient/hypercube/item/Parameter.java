package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class Parameter extends NamedItem {
    public Parameter(JsonObject var) {
        super(var);
    }

    public Parameter(String name) {
        super(name);
    }

    public Parameter() {super();}

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","Parameter");
        object.addProperty("type","any");
        object.addProperty("plural",false);
        object.addProperty("optional",false);
        return object;
    }

    @Override
    public String getId() {
        return "pn_el";
    }

    @Override
    protected Item getIconItem() {
        return Items.ENDER_EYE;
    }
}
