package dev.dfonline.codeclient.hypercube.actiondump;

import net.minecraft.item.ItemStack;

import java.util.List;

public class CodeBlock implements Searchable {
    public String name;
    public String identifier;
    public Icon item;

    @Override
    public List<String> getTerms() {
        return List.of(name, item.getCleanName());
    }

    @Override
    public ItemStack getItem() {
        return item.getItem();
    }
}
