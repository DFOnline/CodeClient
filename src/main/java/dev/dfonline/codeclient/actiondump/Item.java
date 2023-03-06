package dev.dfonline.codeclient.actiondump;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Item {
    public String material;
    public String name;
    public String[] deprecatedNote;
    public String[] description;
    public String[] example;
    public String[] worksWith;
    public String[][] additionalInfo;
    public String requiredRank;
    public String requireTokens;
    public String requireRankAndTokens;
    public boolean advanced;
    public Boolean cancellable;
    public Boolean cancelledAutomatically;
    public String loadedItem;

    public ItemStack getItem() {
        ItemStack item = Registry.ITEM.get(new Identifier(material.toLowerCase())).getDefaultStack();

        NbtCompound nbt = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();

        for (String line: description) {
            addToLore(lore, "§7" + line);
        }
        if(example != null && example.length != 0) {
            addToLore(lore,"");
            addToLore(lore,"Example:");
            for (String line: example) {
                addToLore(lore, "§7" + line);
            }
        }
        if(additionalInfo != null && additionalInfo.length != 0) {
            addToLore(lore,"");
            addToLore(lore,"§9Additional Info:");
            for(String[] group : additionalInfo) {
                int i = 0;
                for(String line: group) {
                    if(i == 0) addToLore(lore, "§b» §7" + line);
                    else addToLore(lore, "§7" + line);
                    i++;
                }
            }
        }
        if(cancellable != null && cancellable) {
            addToLore(lore,"");
            if(cancelledAutomatically) addToLore(lore, "§4∅ §cCancelled automatically");
            else addToLore(lore, "§4∅ §cCancellable");
        }
        display.put("Lore",lore);
        nbt.put("display",display);
        nbt.put("HideFlags", NbtInt.of(127));
        item.setNbt(nbt);

        item.setCustomName(Text.literal("§b" + name));

        return item;
    }

    private void addToLore(NbtList lore, String text) {
        lore.add(NbtString.of("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"white\",\"text\":\"%s\"}],\"text\":\"\"}".formatted(text.replace("\"","\\\""))));
    }
}
