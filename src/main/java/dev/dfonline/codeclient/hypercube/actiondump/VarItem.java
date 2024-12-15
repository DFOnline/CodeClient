package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.ItemStack;

public abstract class VarItem {
    public Icon icon;

    public ItemStack getItem(String id, JsonObject data) {
        ItemStack item = icon.getItem();

        DFItem dfItem = DFItem.of(item);
        dfItem.editData(itemData -> {
            itemData.setHypercubeStringValue("id", id);
            itemData.setHypercubeStringValue("data", data.toString());
        });
        return dfItem.getItemStack();
    }
}
