package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.PublicBukkitValues;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
        DFItem dfItem = DFItem.of(item);
        PublicBukkitValues pbv = dfItem.getPublicBukkitValues();
        String varitem = pbv.getHypercubeStringValue("varitem");
        if (varitem.isEmpty()) throw new Exception("Item does not have a varitem");
        return JsonParser.parseString(varitem).getAsJsonObject();

    }

    public ItemStack getIcon() {
        ItemStack item = getIconItem().getDefaultStack();
        DFItem dfItem = DFItem.of(item);
        dfItem.setCustomModelData(5000);
        return item;
    }

    public ItemStack toStack() {
        DFItem dfItem = DFItem.of(getIcon());
        var varItem = new JsonObject();
        varItem.addProperty("id", getId());
        varItem.add("data", data);
        dfItem.getPublicBukkitValues().setHypercubeStringValue("varitem", varItem.toString());
        return dfItem.getItemStack();
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
