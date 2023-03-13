package dev.dfonline.codeclient.actiondump;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface Searchable {
    List<String> getTerms();
    ItemStack getItem();
}
