package dev.dfonline.codeclient.hypercube.actiondump;

import com.ibm.icu.text.CaseMap;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Argument {
    public String type;
    public boolean plural;
    public boolean optional;
    public String[] description;
    public String[][] notes;
    public String text;

    @Nullable
    public Icon.Type getType() {
        try {
            return Icon.Type.valueOf(type);
        } catch (Exception ignored) {
            return null;
        }
    }

    public ItemStack getItem() {
        Icon.Type type = getType();
        ItemStack item = type == null
                ? Items.DYE.gray().getDefaultInstance()
                : type.getIcon();

        DFItem dfItem = DFItem.of(item);
        // First line is item name, others are lore
        List<Component> lore = getLore();
        if (lore.isEmpty()) return item;
        dfItem.setName(lore.getFirst());
        lore.removeFirst();
        dfItem.setLore(lore);

        return item;
    }

    public List<Component> getLore() {
        ArrayList<Component> lore = new ArrayList<>();
        int i = 0;
        if (this.text != null) addToLore(lore, this.text);
        if (this.description != null) for (String line : this.description) {
            MutableComponent text = Component.empty().withStyle(ChatFormatting.GRAY).withStyle(s -> s.withItalic(false));
            MutableComponent typeText;

            try {
                Icon.Type type = Icon.Type.valueOf(this.type);
                typeText = Component.literal(type.display).setStyle(Component.empty().getStyle().withColor(type.color).withItalic(false));
            } catch (IllegalArgumentException e) {
                // Show a grayed out version of the name.
                String properCase = CaseMap.Title.toTitle().apply(Locale.ENGLISH, null, this.type.replaceAll("_", " "));
                typeText = Component.literal(properCase).setStyle(Component.empty().getStyle()
                        .withColor(TextColor.fromRgb(0x808080))
                        .withItalic(false));
            }

            if (i == 0) {

                if (this.plural) typeText.append("(s)");
                text.append(typeText);
                if (this.optional) {
                    text.append(Component.literal("*").withStyle(ChatFormatting.WHITE));
                }
                text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY));
                text.append(Utility.textFromString(line).withStyle(ChatFormatting.GRAY));
                lore.add(text);
            } else lore.add(Utility.textFromString(line).withStyle(ChatFormatting.GRAY));
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

    public boolean isOr() {
        return text != null && text.endsWith("OR");
    }

    public boolean isSplitter() {
        return Objects.equals(text, "");
    }

    private void addToLore(ArrayList<Component> lore, String text) {
        lore.add(Utility.textFromString(text));
    }
}