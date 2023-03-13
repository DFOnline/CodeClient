package dev.dfonline.codeclient.actiondump;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class Icon {
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
    public Argument[] arguments;

    public ItemStack getItem() {
        ItemStack item = Registries.ITEM.get(new Identifier(material.toLowerCase())).getDefaultStack();

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
        if(arguments != null && arguments.length != 0) {
            addToLore(lore,"");
            addToLore(lore,"Chest Parameters:");
            for (Argument arg: arguments) {
                int i = 0;
                if(arg.text != null) addToLore(lore, arg.text);
                if(arg.description != null && description.length != 0) for (String line: arg.description) {
                    Argument.Type type = Argument.Type.valueOf(arg.type);
                    if(i == 0) lore.add(NbtString.of("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"%s\",\"text\":\"%s\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"white\",\"text\":\"%s\"}],\"text\":\"\"}".formatted(type.color.toString(),type.display + (arg.plural ? "(s)" : ""),(arg.optional ? "§f*" : "") + "§8 - §7" + line)));
                    else addToLore(lore, "§7" + line);
                    i++;
                }
                if(arg.notes != null) for (String[] lines: arg.notes) {
                    i = 0;
                    if(lines != null) for (String line: lines) {
                        if(i == 0) addToLore(lore, "§9⏵ §7" + line);
                        else addToLore(lore, "§7" + line);
                        i++;
                    }
                }

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

        item.setCustomName(Text.of(name));

        return item;
    }

    private void addToLore(NbtList lore, String text) {
        lore.add(NbtString.of("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"white\",\"text\":\"%s\"}],\"text\":\"\"}".formatted(text.replace("\"","\\\""))));
    }
}
