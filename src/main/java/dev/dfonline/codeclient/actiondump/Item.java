package dev.dfonline.codeclient.actiondump;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.rmi.registry.RegistryHandler;

public class Item {
    public String material;
    public String name;
    public String[] deprecatedNote;
    public String[] description;
    public String[] example;
    public String[] worksWith;
    public String requiredRank;
    public String requireTokens;
    public String requireRankAndTokens;
    public boolean advanced;
    public String loadedItem;

    public ItemStack getItem() {
        ItemStack item = Registry.ITEM.get(new Identifier(material.toLowerCase())).getDefaultStack();
        item.setCustomName(Text.literal("§b" + name));
        return item;
    }
}
