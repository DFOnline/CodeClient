package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class BlockTag extends VarItem {
    public BlockTag(Item material, JsonObject var) {
        super(material, var);
    }
}
