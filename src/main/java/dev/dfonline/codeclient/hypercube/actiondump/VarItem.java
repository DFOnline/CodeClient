package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

public abstract class VarItem {
    public Icon icon;

    public ItemStack getItem(String id, JsonObject data) {
        ItemStack item = icon.getItem();
        NbtCompound publicBukkitValues = new NbtCompound();
        JsonObject varItem = new JsonObject();
        varItem.addProperty("id", id);
        varItem.add("data", data);
        publicBukkitValues.put("hypercube:varitem", NbtString.of(varItem.toString()));
        var customData = item.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            customData = NbtComponent.DEFAULT;
        }
        var nbt = customData.copyNbt();
        nbt.put("PublicBukkitValues", publicBukkitValues);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return item;
    }
}
