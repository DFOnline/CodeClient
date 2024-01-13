package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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
    public Integer tags;
    public Argument[] arguments;
    public ReturnValue[] returnValues;

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
            boolean hasOptional = false;
            for (Argument arg: arguments) {
                int i = 0;
                if(arg.text != null) addToLore(lore, arg.text);
                if(arg.description != null && description.length != 0) for (String line: arg.description) {
                    Type type = Type.valueOf(arg.type);
                    if(i == 0) {
                        MutableText text = Text.empty().formatted(Formatting.GRAY);
                        MutableText typeText = Text.literal(type.display).setStyle(Text.empty().getStyle().withColor(type.color));
                        if(arg.plural) typeText.append("(s)");
                        text.append(typeText);
                        if(arg.optional) {
                            text.append(Text.literal("*").formatted(Formatting.WHITE));
                            hasOptional = true;
                        }
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
            if(tags != null && tags != 0) {
                lore.add(Utility.nbtify(Text.literal("# ").formatted(Formatting.DARK_AQUA).append(Text.literal(tags + " Tag" + (tags != 1 ? "s" : "")).formatted(Formatting.GRAY))));
            }
            if(hasOptional) {
                lore.add(Utility.nbtify(Text.literal("")));
                lore.add(Utility.nbtify(Text.literal("*Optional").formatted(Formatting.GRAY)));
            }
        }
        if(returnValues != null && returnValues.length != 0) {
            addToLore(lore,"");
            addToLore(lore,"Returns Value:");
            for (ReturnValue returnValue: returnValues) {
                if(returnValue.text != null) addToLore(lore,returnValue.text);
                else {
                    lore.add(Utility.nbtify(Text.empty().append(Text.literal(returnValue.type.display).setStyle(Style.EMPTY.withColor(returnValue.type.color))).append(Text.literal(" - ").formatted(Formatting.DARK_GRAY)).append(Text.literal(returnValue.description[0]).formatted(Formatting.GRAY))));
                    boolean first = true;
                    for (String description: returnValue.description) {
                        if(first) {
                            first = false;
                            continue;
                        }
                        addToLore(lore,"§7" + description);
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

    private static TextColor GOLD = TextColor.fromFormatting(Formatting.GOLD);
    public enum Type {
        TEXT(TextColor.fromFormatting(Formatting.AQUA), "Text"),
        COMPONENT(TextColor.fromRgb(0x7fd42a), "Rich Text"),
        NUMBER(TextColor.fromFormatting(Formatting.RED), "§cNumber"),
        LOCATION(TextColor.fromFormatting(Formatting.GREEN), "§aLocation"),
        VECTOR(TextColor.fromRgb(0x2AFFAA), "Vector"),
        SOUND(TextColor.fromFormatting(Formatting.BLUE), "Sound"),
        PARTICLE(TextColor.fromRgb(0xAA55FF), "Particle Effect"),
        POTION(TextColor.fromRgb(0xFF557F), "Potion Effect"),
        VARIABLE(TextColor.fromFormatting(Formatting.YELLOW), "Variable"),
        ANY_TYPE(TextColor.fromRgb(0xFFD47F), "Any Value"),
        ITEM(GOLD, "Item"),
        BLOCK(GOLD, "Block"),
        ENTITY_TYPE(GOLD, "Entity Type"),
        SPAWN_EGG(GOLD, "Spawn Egg"),
        VEHICLE(GOLD, "Vehicle"),
        PROJECTILE(GOLD, "Projectile"),
        BLOCK_TAG(TextColor.fromFormatting(Formatting.AQUA), "Block Tag"),
        LIST(TextColor.fromFormatting(Formatting.DARK_GREEN), "List"),
        DICT(TextColor.fromRgb(0x55AAFF), "Dictionary"),
        NONE(TextColor.fromRgb(0x808080), "None"),
        ;

        public final TextColor color;
        public final String display;
        Type(TextColor color, String display) {
            this.color = color;
            this.display = display;
        }
    }

    record ReturnValue(Type type, String[] description, String text) {}

    private void addToLore(NbtList lore, String text) {
        lore.add(Utility.nbtify(Utility.textFromString(text)));
    }

    public static class Color {
        int red;
        int green;
        int blue;

        public int getColor() {
            return (red << 16) + (green << 8) + blue;
        }
    }
}
