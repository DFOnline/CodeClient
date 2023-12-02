package dev.dfonline.codeclient.hypercube.item;

import net.minecraft.item.ItemStack;

public class NamedItem extends VarItem {
    private String name;

    public NamedItem(ItemStack item) throws Exception {
        super(item);
        this.name = this.data.get("name").getAsString();
    }

    @Override
    public ItemStack toStack() {
        return super.toStack();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.data.addProperty("name",name);
        this.name = name;
    }
}
