package dev.dfonline.codeclient.actiondump;

import net.minecraft.item.ItemStack;

public class Particle {
    public String particle;
    public String category;
    public Icon icon;
    public String[] fields;

    public ItemStack getItem() {
        return icon.getItem();
    }
}
