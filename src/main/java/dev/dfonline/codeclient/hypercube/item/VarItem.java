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
    // TODO: make it abstract and final, it's that's even possible, and be decided by whatever implements it. This does also mean implementing each named item
    protected Item material;

    protected VarItem(String id) {
        this.id = id;
    }

    public static JsonObject prefetch(ItemStack item) throws Exception {
        if (!item.hasNbt()) throw new Exception("Item has no nbt.");
        NbtCompound nbt = item.getNbt();
        if (nbt == null) throw new Exception("NBT is null.");
        if (!nbt.contains("PublicBukkitValues",NbtElement.COMPOUND_TYPE)) throw new Exception("Item has no PublicBukkitValues");
        NbtCompound publicBukkit = nbt.getCompound("PublicBukkitValues");
        if (!publicBukkit.contains("hypercube:varitem", NbtElement.STRING_TYPE)) throw new Exception("Item has no hypercube:varitem");
        String varitem = publicBukkit.getString("hypercube:varitem");
        return JsonParser.parseString(varitem).getAsJsonObject();

    }
    public VarItem(ItemStack item) throws Exception {
        this(item.getItem(), prefetch(item));
    }

    public VarItem(Item material, JsonObject var) {
        this.material = material;
        id = var.get("id").getAsString();
        data = var.get("data").getAsJsonObject();
    }


    public ItemStack toStack() {
        var pbv = new NbtCompound();
        var varItem = new JsonObject();
        varItem.addProperty("id",id);
        varItem.add("data",data);
        pbv.put("hypercube:varitem", NbtString.of(varItem.toString()));
        ItemStack item = material.getDefaultStack();
        item.setSubNbt("PublicBukkitValues",pbv);
        return item;
    }

    public JsonObject getData() {
        return data;
    }

    public JsonObject getVar() {
        // This is probably the best approach tbh instead of updating data constantly
        var var = new JsonObject();
        var.addProperty("id",id);
        var.add("data",getData());
        return var;
    }
}
