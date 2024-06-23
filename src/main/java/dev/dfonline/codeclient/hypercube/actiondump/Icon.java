package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.hypercube.item.Number;
import dev.dfonline.codeclient.hypercube.item.Potion;
import dev.dfonline.codeclient.hypercube.item.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Icon {
    private static TextColor GOLD = TextColor.fromFormatting(Formatting.GOLD);
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
     *
     * @return
     */
    public String getCleanName() {
        return this.name.replaceAll("§.", "");
    }

    public ItemStack getItem() {
        ItemStack item = Registries.ITEM.get(new Identifier(material.toLowerCase())).getDefaultStack();

        NbtCompound nbt = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();

        for (String line : description) {
            addToLore(lore, "§7" + line);
        }
        if (example != null && example.length != 0) {
            addToLore(lore, "");
            addToLore(lore, "Example:");
            for (String line : example) {
                addToLore(lore, "§7" + line);
            }
        }
        if (arguments != null && arguments.length != 0) {
            addToLore(lore, "");
            addToLore(lore, "Chest Parameters:");
            boolean hasOptional = false;
            for (Argument arg : arguments) {
                int i = 0;
                if (arg.text != null) addToLore(lore, arg.text);
                if (arg.description != null && description.length != 0) for (String line : arg.description) {
                    Type type = Type.valueOf(arg.type);
                    if (i == 0) {
                        MutableText text = Text.empty().formatted(Formatting.GRAY);
                        MutableText typeText = Text.literal(type.display).setStyle(Text.empty().getStyle().withColor(type.color));
                        if (arg.plural) typeText.append("(s)");
                        text.append(typeText);
                        if (arg.optional) {
                            text.append(Text.literal("*").formatted(Formatting.WHITE));
                            hasOptional = true;
                        }
                        text.append(Text.literal(" - ").formatted(Formatting.DARK_GRAY));
                        text.append(Utility.textFromString(line).formatted(Formatting.GRAY));
                        lore.add(Utility.nbtify(text));
                    } else lore.add(Utility.nbtify(Utility.textFromString(line).formatted(Formatting.GRAY)));
                    i++;
                }
                if (arg.notes != null) for (String[] lines : arg.notes) {
                    i = 0;
                    if (lines != null) for (String line : lines) {
                        if (i == 0) addToLore(lore, "§9⏵ §7" + line);
                        else addToLore(lore, "§7" + line);
                        i++;
                    }
                }
            }
            if (tags != null && tags != 0) {
                lore.add(Utility.nbtify(Text.literal("# ").formatted(Formatting.DARK_AQUA).append(Text.literal(tags + " Tag" + (tags != 1 ? "s" : "")).formatted(Formatting.GRAY))));
            }
            if (hasOptional) {
                lore.add(Utility.nbtify(Text.literal("")));
                lore.add(Utility.nbtify(Text.literal("*Optional").formatted(Formatting.GRAY)));
            }
        }
        if (returnValues != null && returnValues.length != 0) {
            addToLore(lore, "");
            addToLore(lore, "Returns Value:");
            for (ReturnValue returnValue : returnValues) {
                if (returnValue.text != null) addToLore(lore, returnValue.text);
                else {
                    lore.add(Utility.nbtify(Text.empty().append(Text.literal(returnValue.type.display).setStyle(Style.EMPTY.withColor(returnValue.type.color))).append(Text.literal(" - ").formatted(Formatting.DARK_GRAY)).append(Text.literal(returnValue.description[0]).formatted(Formatting.GRAY))));
                    boolean first = true;
                    for (String description : returnValue.description) {
                        if (first) {
                            first = false;
                            continue;
                        }
                        addToLore(lore, "§7" + description);
                    }
                }
            }
        }
        if (additionalInfo != null && additionalInfo.length != 0) {
            addToLore(lore, "");
            addToLore(lore, "§9Additional Info:");
            for (String[] group : additionalInfo) {
                int i = 0;
                for (String line : group) {
                    if (i == 0) addToLore(lore, "§b» §7" + line);
                    else addToLore(lore, "§7" + line);
                    i++;
                }
            }
        }
        if (cancellable != null && cancellable) {
            addToLore(lore, "");
            if (cancelledAutomatically) addToLore(lore, "§4∅ §cCancelled automatically");
            else addToLore(lore, "§4∅ §cCancellable");
        }
        display.put("Lore", lore);

        display.put("Name", Utility.nbtify(Utility.textFromString(name)));

        nbt.put("display", display);
        nbt.put("HideFlags", NbtInt.of(127));
        if (color != null) nbt.put("CustomPotionColor", NbtInt.of(color.getColor()));

        if (head != null) {
            NbtCompound SkullOwner = new NbtCompound();
            NbtIntArray Id = new NbtIntArray(List.of(0, 0, 0, 0));
            SkullOwner.put("Id", Id);
            SkullOwner.putString("Name", "DF-HEAD");
            NbtCompound Properties = new NbtCompound();
            NbtList textures = new NbtList();
            NbtCompound texture = new NbtCompound();
            texture.putString("Value", head);
            textures.add(texture);
            Properties.put("textures", textures);
            SkullOwner.put("Properties", Properties);
            nbt.put("SkullOwner", SkullOwner);
        }

        item.setNbt(nbt);
        return item;
    }

    private void addToLore(NbtList lore, String text) {
        lore.add(Utility.nbtify(Utility.textFromString(text)));
    }

    public List<ArgumentGroup> getArgGroups() {
        var groups = new ArrayList<ArgumentGroup>();
        var group = new ArrayList<Argument>();
        for(var arg: arguments) {
            if(!arg.isSplitter()) group.add(arg);
            else {
                groups.add(new ArgumentGroup(group));
                group = new ArrayList<>();
            }
        }
        groups.add(new ArgumentGroup(group));
        return groups;
    }

    public enum Type {
        TEXT(TextColor.fromFormatting(Formatting.AQUA), "String", Items.STRING, dev.dfonline.codeclient.hypercube.item.Text::new),
        COMPONENT(TextColor.fromRgb(0x7fd42a), "Styled Text", Items.BOOK, Component::new),
        NUMBER(TextColor.fromFormatting(Formatting.RED), "Number", Items.SLIME_BALL, Number::new),
        LOCATION(TextColor.fromFormatting(Formatting.GREEN), "Location", Items.PAPER, Location::new),
        VECTOR(TextColor.fromRgb(0x2AFFAA), "Vector", Items.PRISMARINE_SHARD, Vector::new),
        SOUND(TextColor.fromFormatting(Formatting.BLUE), "Sound", Items.NAUTILUS_SHELL, Sound::new),
        PARTICLE(TextColor.fromRgb(0xAA55FF), "Particle Effect", Items.WHITE_DYE),
        POTION(TextColor.fromRgb(0xFF557F), "Potion Effect", Items.DRAGON_BREATH, Potion::new),
        VARIABLE(TextColor.fromFormatting(Formatting.YELLOW), "Variable", Items.MAGMA_CREAM, Variable::new),
        ANY_TYPE(TextColor.fromRgb(0xFFD47F), "Any Value", Items.POTATO),
        ITEM(GOLD, "Item", Items.ITEM_FRAME, dev.dfonline.codeclient.hypercube.item.Text::new),
        BLOCK(GOLD, "Block", Items.OAK_LOG, dev.dfonline.codeclient.hypercube.item.Text::new),
        ENTITY_TYPE(GOLD, "Entity Type", Items.PIG_SPAWN_EGG),
        SPAWN_EGG(GOLD, "Spawn Egg", Items.POLAR_BEAR_SPAWN_EGG), // Least consistent in df, all of sheep, polar bear, phantom, pig
        VEHICLE(GOLD, "Vehicle", Items.OAK_BOAT),
        PROJECTILE(GOLD, "Projectile", Items.ARROW),
        BLOCK_TAG(TextColor.fromFormatting(Formatting.AQUA), "Block Tag", Items.CHAIN_COMMAND_BLOCK, dev.dfonline.codeclient.hypercube.item.Text::new),
        LIST(TextColor.fromFormatting(Formatting.DARK_GREEN), "List", Items.SKULL_BANNER_PATTERN), // Ender chest or empty banner pattern
        DICT(TextColor.fromRgb(0x55AAFF), "Dictionary", Items.KNOWLEDGE_BOOK), // Knowledge book or chest minecart
        NONE(TextColor.fromRgb(0x808080), "None", Items.AIR),
        ;

        public final TextColor color;
        public final String display;
        private final ItemStack icon;
        @Nullable public final Icon.Type.getVarItem getVarItem;

        Type(TextColor color, String display, Item icon) {
            this.color = color;
            this.display = display;
            var item = icon.getDefaultStack();
            item.setSubNbt("CustomModelData",NbtInt.of(5000));
            this.icon = item;
            getVarItem = null;
        }

        public interface getVarItem {
            VarItem run();
        }

        Type(TextColor color, String display, Item icon, @Nullable Icon.Type.getVarItem getVarItem) {
            this.color = color;
            this.display = display;
            var item = icon.getDefaultStack();
            item.setSubNbt("CustomModelData",NbtInt.of(5000));
            this.icon = item;
            this.getVarItem = getVarItem;
        }

        public ItemStack getIcon() {
            return icon.copy();
        }
    }

    public record ArgumentGroup(List<Argument> arguments) {
        public List<ArgumentPossibilities> getPossibilities() {
            var groups = new ArrayList<ArgumentPossibilities>();
            var group = new ArrayList<Argument>();
            for(var arg: arguments) {
                if(!arg.isOr()) group.add(arg);
                else {
                    groups.add(new ArgumentPossibilities(group));
                    group = new ArrayList<>();
                }
            }
            groups.add(new ArgumentPossibilities(group));
            return groups;
        }

        public record ArgumentPossibilities(List<Argument> arguments) {

        }
    }

    record ReturnValue(Type type, String[] description, String text) {
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
