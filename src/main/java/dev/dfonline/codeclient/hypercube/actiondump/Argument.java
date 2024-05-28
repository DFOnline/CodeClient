package dev.dfonline.codeclient.hypercube.actiondump;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Argument {
    public String type;
    public boolean plural;
    public boolean optional;
    public String[] description;
    public String[][] notes;
    public String text;

    @Nullable
    public Icon.Type getType() {
        try {
            return Icon.Type.valueOf(type);
        } catch (Exception ignored) {
            return null;
        }
    }

    public ItemStack getItem() {
        Icon.Type type = getType();
        if(type == null) return ItemStack.EMPTY;
        return type.getIcon();
    }
}