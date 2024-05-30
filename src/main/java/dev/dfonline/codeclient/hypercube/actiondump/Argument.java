package dev.dfonline.codeclient.hypercube.actiondump;

import dev.dfonline.codeclient.Utility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class Argument {
    public String type;
    public boolean plural;
    public boolean optional;
    public String[] description;
    public String[][] notes;
    public String text;

    public static final Argument SPLITTER;
    public static final Argument OR;

    static {
        var splitter = new Argument();
        splitter.text = "";
        SPLITTER = splitter;

        var or = new Argument();
        or.text = "§x§f§f§5§5§a§aOR";
        OR = or;
    }

    @Nullable
    public Icon.Type getType() {
        try {
            return Icon.Type.valueOf(type);
        } catch (Exception ignored) {
            return null;
        }
    }

    public ItemStack getItem() {
        Icon.Type icon = getType();
        if(icon == null) return ItemStack.EMPTY;
        var item = icon.getIcon();

        NbtCompound nbt = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();

        int index = 0;
        for (NbtElement line: getLore()) {
            if(line instanceof NbtString string) {
                if(index++ == 0) {
                    display.put("Name", string);
                }
                else {
                    lore.add(line);
                }
            }
        }

        display.put("Lore",lore);

        nbt.put("display", display);
        nbt.put("HideFlags", NbtInt.of(127));

        item.setNbt(nbt);

        return item;
    }
    
    public NbtList getLore() {
        var lore = new NbtList();
        int i = 0;
        if (this.text != null) addToLore(lore, this.text);
        if (this.description != null) for (String line : this.description) {
            Icon.Type type = Icon.Type.valueOf(this.type);
            if (i == 0) {
                MutableText text = Text.empty().formatted(Formatting.GRAY);
                MutableText typeText = Text.literal(type.display).setStyle(Text.empty().getStyle().withColor(type.color));
                if (this.plural) typeText.append("(s)");
                text.append(typeText);
                if (this.optional) {
                    text.append(Text.literal("*").formatted(Formatting.WHITE));
                }
                text.append(Text.literal(" - ").formatted(Formatting.DARK_GRAY));
                text.append(Utility.textFromString(line).formatted(Formatting.GRAY));
                lore.add(Utility.nbtify(text));
            } else lore.add(Utility.nbtify(Utility.textFromString(line).formatted(Formatting.GRAY)));
            i++;
        }
        if (this.notes != null) for (String[] lines : this.notes) {
            i = 0;
            if (lines != null) for (String line : lines) {
                if (i == 0) addToLore(lore, "§9⏵ §7" + line);
                else addToLore(lore, "§7" + line);
                i++;
            }
        }

        return lore;
    }

    private void addToLore(NbtList lore, String text) {
        lore.add(Utility.nbtify(Utility.textFromString(text)));
    }}