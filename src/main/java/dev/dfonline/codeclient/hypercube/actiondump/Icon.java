package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class Icon {
    public String material;
    public String head;
    public String name;
    public Color color;
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

    /**
     * Gets the pure name, without any color codes.
     * @return
     */
    public String getCleanName() {
        return this.name.replaceAll("§.","");
    }

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
                    if(i == 0) {
                        MutableText text = Text.empty().formatted(Formatting.GRAY);
                        MutableText typeText = Text.literal(type.display).setStyle(Text.empty().getStyle().withColor(type.color));
                        if(arg.plural) typeText.append("(s)");
                        text.append(typeText);
                        if(arg.optional) text.append(Text.literal("*").formatted(Formatting.WHITE));
                        text.append(Text.literal(" - ").formatted(Formatting.DARK_GRAY));
                        text.append(Utility.textFromString(line).formatted(Formatting.GRAY));
                        lore.add(Utility.nbtify(text));
                    }
                    else lore.add(Utility.nbtify(Utility.textFromString(line).formatted(Formatting.GRAY)));
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

        display.put("Name",Utility.nbtify(Utility.textFromString(name)));

        nbt.put("display",display);
        nbt.put("HideFlags", NbtInt.of(127));
        if(color != null) nbt.put("CustomPotionColor", NbtInt.of(color.getColor()));

        if(head != null) {
            NbtCompound SkullOwner = new NbtCompound();
                NbtIntArray Id = new NbtIntArray(List.of(0,0,0,0));
                SkullOwner.put("Id",Id);
                SkullOwner.putString("Name","DF-HEAD");
                NbtCompound Properties = new NbtCompound();
                    NbtList textures = new NbtList();
                        NbtCompound texture = new NbtCompound();
                            texture.putString("Value",head);
                    textures.add(texture);
                    Properties.put("textures",textures);
                SkullOwner.put("Properties",Properties);
            nbt.put("SkullOwner",SkullOwner);
        }

        item.setNbt(nbt);
        return item;
    }

    private void addToLore(NbtList lore, String text) {
        lore.add(Utility.nbtify(Utility.textFromString(text)));
    }

    private class Color {
        int red;
        int green;
        int blue;

        public int getColor() {
            return (red << 16) + (green << 8) + blue;
        }
    }
}
