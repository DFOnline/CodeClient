package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public abstract class VarItem {
    public final String id;
    protected JsonObject data;
    protected Item material;

    public VarItem(ItemStack item) throws Exception {
        material = item.getItem();
        if (!item.hasNbt()) throw new Exception("Item has no nbt.");
        NbtCompound nbt = item.getNbt();
        if (nbt == null) throw new Exception("NBT is null.");
        if (!nbt.contains("PublicBukkitValues",NbtElement.COMPOUND_TYPE)) throw new Exception("Item has no PublicBukkitValues");
        NbtCompound publicBukkit = nbt.getCompound("PublicBukkitValues");
        if (!publicBukkit.contains("hypercube:varitem", NbtElement.STRING_TYPE)) throw new Exception("Item has no hypercube:varitem");
        String varitem = publicBukkit.getString("hypercube:varitem");
        JsonObject var = JsonParser.parseString(varitem).getAsJsonObject();
        id = var.get("id").getAsString();
        data = var.get("data").getAsJsonObject();
    }

    public ItemStack toStack() {
        var pbv = new NbtCompound();
        var varitem = new JsonObject();
        varitem.addProperty("id",id);
        varitem.add("data",data);
        pbv.put("hypercube:varitem", NbtString.of(varitem.toString()));
        ItemStack item = material.getDefaultStack();
        item.setSubNbt("PublicBukkitValues",pbv);
        return item;
    }

    public JsonObject getData() {
        return data;
    }
}
