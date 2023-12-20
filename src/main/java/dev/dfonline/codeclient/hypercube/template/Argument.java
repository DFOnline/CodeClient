package dev.dfonline.codeclient.hypercube.template;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public class Argument {
    public int slot;
    @NotNull public VarItem item;
    public Argument(@NotNull VarItem item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.add("item",item.getVar());
        object.addProperty("slot",slot);
        return object;
    }

    /**
     * Parse from a real in-game item.
     */
    public Argument(@NotNull ItemStack item, int slot) {
        this(getVarItem(item), slot);
    }

    public static VarItem getVarItem(@NotNull ItemStack stack) {
        var parsed = VarItems.parse(stack);
        if(parsed != null) return parsed;
        return new MinecraftItem(stack);
    }

    public static class MinecraftItem extends VarItem {
        public @NotNull ItemStack stack;
        public MinecraftItem(@NotNull ItemStack item) {
            super("item");
            stack = item;
        }

        @Override
        public JsonObject getData() {
            var data = new JsonObject();
            var item = new NbtCompound();
            stack.writeNbt(item);
            data.addProperty("item",item.toString());
            return data;
        }
        public void setStack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack toStack() {
            return super.toStack();
        }
    }
}
