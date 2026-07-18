package dev.dfonline.codeclient.hypercube.template;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class Argument {
    public int slot;
    @NotNull
    public VarItem item;

    public Argument(@NotNull VarItem item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    /**
     * Parse from a real in-game item.
     */
    public Argument(@NotNull ItemStack item, int slot) {
        this(getVarItem(item), slot);
    }

    public static VarItem getVarItem(@NotNull ItemStack stack) {
        var parsed = VarItems.parse(stack);
        if (parsed != null) return parsed;
        return new MinecraftItem(stack);
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.add("item", item.getVar());
        object.addProperty("slot", slot);
        return object;
    }

    public static class MinecraftItem extends VarItem {
        public @NotNull ItemStack stack;

        @Override
        public String getId() {
            return "item";
        }

        @Override
        protected Item getIconItem() {
            return Items.ITEM_FRAME;
        }

        @Override
        public JsonObject getDefaultData() {
            return null;
        }

        public MinecraftItem(@NotNull ItemStack item) {
            super();
            stack = item;
        }

        @Override
        public JsonObject getData() {
            DFItem dfItem = DFItem.of(stack);
            dfItem.removeItemData();
            var data = new JsonObject();
            var item = new CompoundTag();
            data.addProperty("item", item.toString());
            return data;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack toStack() {
            return stack;
        }
    }
}
