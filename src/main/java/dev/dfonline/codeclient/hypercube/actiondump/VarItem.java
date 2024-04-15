package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

public abstract class VarItem {
    public Icon icon;

    public ItemStack getItem(String id, JsonObject data) {
        ItemStack item = icon.getItem();
        NbtCompound nbt = item.getNbt();
        NbtCompound publicBukkitValues = new NbtCompound();
        JsonObject varItem = new JsonObject();
        varItem.addProperty("id", id);
        varItem.add("data", data);
        publicBukkitValues.put("hypercube:varitem", NbtString.of(varItem.toString()));
        nbt.put("PublicBukkitValues", publicBukkitValues);
        item.setNbt(nbt);
        return item;
    }
}
