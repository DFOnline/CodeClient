package dev.dfonline.codeclient.actiondump;

import net.minecraft.item.ItemStack;

import java.util.List;

public class CodeBlock implements Searchable {
    public String name;
    public String identifier;
    public Icon item;

    @Override
    public List<String> getTerms() {
        return List.of(name, item.getName());
    }

    @Override
    public ItemStack getItem() {
        return item.getItem();
    }
}
