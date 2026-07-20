package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.data.ItemData;
import dev.dfonline.codeclient.hypercube.item.Location;
import dev.dfonline.codeclient.hypercube.item.Number;
import dev.dfonline.codeclient.hypercube.item.Potion;
import dev.dfonline.codeclient.hypercube.item.Sound;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.Variable;
import dev.dfonline.codeclient.hypercube.item.Vector;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Icon {
    private static final TextColor GOLD = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
    public String material;
    public String head;
    public String name;
    public Color color;
    public String[] deprecatedNote;
    public String[] description;
    public String[] example;
    public String[] worksWith;
    public String[][] additionalInfo;
    public RequiredRank requiredRank;
    public boolean requireTokens;
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
        ItemStack item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material.toLowerCase())).getDefaultInstance();

        DFItem dfItem = DFItem.of(item);
        ItemData data = dfItem.getItemData();

        ArrayList<Component> lore = new ArrayList<>();

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
                lore.addAll(arg.getLore());
            }
            if (tags != null && tags != 0) {
                lore.add(Component.literal("# ").withStyle(ChatFormatting.DARK_AQUA).withStyle(s -> s.withItalic(false)).append(Component.literal(tags + " Tag" + (tags != 1 ? "s" : "")).withStyle(ChatFormatting.GRAY)));
            }
            if (hasOptional) {
                lore.add(Component.empty());
                lore.add(Component.literal("*Optional").withStyle(ChatFormatting.GRAY).withStyle(s -> s.withItalic(false)));
            }
        }
        if (returnValues != null && returnValues.length != 0) {
            addToLore(lore, "");
            addToLore(lore, "Returns Value:");
            for (ReturnValue returnValue : returnValues) {
                if (returnValue.text != null) addToLore(lore, returnValue.text);
                else {
                    lore.add(Component.empty().append(Component.literal(returnValue.type.display).setStyle(Style.EMPTY.withColor(returnValue.type.color).withItalic(false))).append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal(returnValue.description[0]).withStyle(ChatFormatting.GRAY).withStyle(s -> s.withItalic(false))));
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
        if (requireTokens) {
            addToLore(lore, "");
            lore.add(Component.literal("Unlock with Tokens").withColor(0xffd42a));
        }
        if (requiredRank != null) {
            if (requireTokens) {
                lore.add(Component.literal("OR").withColor(0xff55aa));
                lore.add(Component.literal("Unlock with " + requiredRank.name).withColor(requiredRank.color.getValue()));
            } else {
                addToLore(lore, "");
                lore.add(Component.literal(requiredRank.name + " Exclusive").setStyle(Style.EMPTY.withColor(requiredRank.color.getValue()).withItalic(false)));
            }
        }

        dfItem.setName(Utility.textFromString(name));
        dfItem.setLore(lore);

        dfItem.hideFlags();

        if (color != null) dfItem.setDyeColor(color.getColor());

        if (head != null) dfItem.setProfile(Util.NIL_UUID, head, null);

        dfItem.setCustomModelData(5000);

        return item;
    }

    private void addToLore(ArrayList<Component> lore, String text) {
        lore.add(Utility.textFromString(text));
    }

    public List<ArgumentGroup> getArgGroups() {
        var groups = new ArrayList<ArgumentGroup>();
        var group = new ArrayList<Argument>();
        for (var arg : arguments) {
            if (!arg.isSplitter()) group.add(arg);
            else {
                groups.add(new ArgumentGroup(group));
                group = new ArrayList<>();
            }
        }
        groups.add(new ArgumentGroup(group));
        return groups;
    }

    public enum Type {
        TEXT(TextColor.fromLegacyFormat(ChatFormatting.AQUA), "String", Items.STRING, dev.dfonline.codeclient.hypercube.item.Text::new),
        COMPONENT(TextColor.fromRgb(0x7fd42a), "Styled Text", Items.BOOK, dev.dfonline.codeclient.hypercube.item.Component::new),
        NUMBER(TextColor.fromLegacyFormat(ChatFormatting.RED), "Number", Items.SLIME_BALL, Number::new),
        BYTE(TextColor.fromLegacyFormat(ChatFormatting.RED), "Byte", Items.SLIME_BALL, Number::new),
        LOCATION(TextColor.fromLegacyFormat(ChatFormatting.GREEN), "Location", Items.PAPER, Location::new),
        VECTOR(TextColor.fromRgb(0x2AFFAA), "Vector", Items.PRISMARINE_SHARD, Vector::new),
        SOUND(TextColor.fromLegacyFormat(ChatFormatting.BLUE), "Sound", Items.NAUTILUS_SHELL, Sound::new),
        PARTICLE(TextColor.fromRgb(0xAA55FF), "Particle Effect", Items.DYE.white()),
        POTION(TextColor.fromRgb(0xFF557F), "Potion Effect", Items.DRAGON_BREATH, Potion::new),
        VARIABLE(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), "Variable", Items.MAGMA_CREAM, Variable::new),
        ANY_TYPE(TextColor.fromRgb(0xFFD47F), "Any Value", Items.POTATO),
        ITEM(GOLD, "Item", Items.ITEM_FRAME, dev.dfonline.codeclient.hypercube.item.Text::new),
        BLOCK(GOLD, "Block", Items.OAK_LOG, dev.dfonline.codeclient.hypercube.item.Text::new),
        ENTITY_TYPE(GOLD, "Entity Type", Items.PIG_SPAWN_EGG),
        SPAWN_EGG(GOLD, "Spawn Egg", Items.POLAR_BEAR_SPAWN_EGG), // Least consistent in df, all of sheep, polar bear, phantom, pig
        VEHICLE(GOLD, "Vehicle", Items.OAK_BOAT),
        PROJECTILE(GOLD, "Projectile", Items.ARROW),
        BLOCK_TAG(TextColor.fromLegacyFormat(ChatFormatting.AQUA), "Block Tag", Items.CHAIN_COMMAND_BLOCK, dev.dfonline.codeclient.hypercube.item.Text::new),
        LIST(TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN), "List", Items.SKULL_BANNER_PATTERN), // Ender chest or empty banner pattern
        DICT(TextColor.fromRgb(0x55AAFF), "Dictionary", Items.KNOWLEDGE_BOOK), // Knowledge book or chest minecart
        NONE(TextColor.fromRgb(0x808080), "None", Items.AIR)
        ;

        public final TextColor color;
        public final String display;
        private final ItemStack icon;
        @Nullable
        public final Supplier<VarItem> getVarItem;

        Type(TextColor color, String display, Item icon) {
            this.color = color;
            this.display = display;
            ItemStack item = icon.getDefaultInstance();
            this.icon = item;
            getVarItem = null;
        }

        Type(TextColor color, String display, Item icon, @Nullable Supplier<VarItem> getVarItem) {
            this.color = color;
            this.display = display;
            this.icon = icon.getDefaultInstance();
            this.getVarItem = getVarItem;
        }

        public ItemStack getIcon() {
            return icon.copy();
        }
    }

    public enum RequiredRank {
        Noble("Noble", TextColor.fromRgb(0x7fff7f)),
        Emperor("Emperor", TextColor.fromRgb(0x55aaff)),
        Mythic("Mythic", TextColor.fromRgb(0xd42ad4)),
        Overlord("Overlord", TextColor.fromLegacyFormat(ChatFormatting.RED)),
        Dev("", TextColor.fromRgb(0));

        public final String name;
        public final TextColor color;

        RequiredRank(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }
    }

    public record ArgumentGroup(List<Argument> arguments) {
        public List<ArgumentPossibilities> getPossibilities() {
            var groups = new ArrayList<ArgumentPossibilities>();
            var group = new ArrayList<Argument>();
            for (var arg : arguments) {
                if (!arg.isOr()) group.add(arg);
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

    public record ReturnValue(Type type, String[] description, String text) {
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
