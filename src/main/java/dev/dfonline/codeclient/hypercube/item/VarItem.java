package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;

import java.util.Objects;

public abstract class VarItem {
    protected JsonObject data;
    // TODO: make it abstract and final, it's that's even possible, and be decided by whatever implements it. This does also mean implementing each named item
    public abstract String getId();
    protected abstract Item getIconItem();
    public abstract JsonObject getDefaultData();

    public VarItem(ItemStack item) throws Exception {
        this(prefetch(item));
    }

    public VarItem(JsonObject var) {
        var id = var.get("id").getAsString();
        if(!Objects.equals(id, getId())) throw new IllegalArgumentException("Given wrong id, expecting (%s) but expected (%s)".formatted(id, getId()));
        data = var.get("data").getAsJsonObject();
    }

    public VarItem() {
        data = getDefaultData();
    }

    public static JsonObject prefetch(ItemStack item) throws Exception {
        if (!item.hasNbt()) throw new Exception("Item has no nbt.");
        NbtCompound nbt = item.getNbt();
        if (nbt == null) throw new Exception("NBT is null.");
        if (!nbt.contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE))
            throw new Exception("Item has no PublicBukkitValues");
        NbtCompound publicBukkit = nbt.getCompound("PublicBukkitValues");
        if (!publicBukkit.contains("hypercube:varitem", NbtElement.STRING_TYPE))
            throw new Exception("Item has no hypercube:varitem");
        String varitem = publicBukkit.getString("hypercube:varitem");
        return JsonParser.parseString(varitem).getAsJsonObject();

    }

    public ItemStack getIcon() {
        ItemStack item = getIconItem().getDefaultStack();
        item.setSubNbt("CustomModelData", NbtInt.of(5000));
        return item;
    }

    public ItemStack toStack() {
        var pbv = new NbtCompound();
        var varItem = new JsonObject();
        varItem.addProperty("id", getId());
        varItem.add("data", data);
        pbv.put("hypercube:varitem", NbtString.of(varItem.toString()));
        var item = getIcon();
        item.setSubNbt("PublicBukkitValues", pbv);
        return item;
    }

    public JsonObject getData() {
        return data;
    }

    public JsonObject getVar() {
        // This is probably the best approach tbh instead of updating data constantly
        var var = new JsonObject();
        var.addProperty("id", getId());
        var.add("data", getData());
        return var;
    }
}
