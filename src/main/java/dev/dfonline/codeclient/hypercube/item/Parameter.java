package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class Parameter extends VarItem {
    public Parameter(Item material, JsonObject var) {
        super(material, var);
    }
}
