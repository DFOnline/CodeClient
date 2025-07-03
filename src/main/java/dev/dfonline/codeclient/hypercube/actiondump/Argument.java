package dev.dfonline.codeclient.hypercube.actiondump;

import com.ibm.icu.text.CaseMap;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
                ? Items.GRAY_DYE.getDefaultStack()
                : type.getIcon();

        DFItem dfItem = DFItem.of(item);
        // First line is item name, others are lore
        List<Text> lore = getLore();
        if (lore.isEmpty()) return item;
        dfItem.setName(lore.getFirst());
        lore.removeFirst();
        dfItem.setLore(lore);

        return item;
    }

    public List<Text> getLore() {
        ArrayList<Text> lore = new ArrayList<>();
        int i = 0;
        if (this.text != null) addToLore(lore, this.text);
        if (this.description != null) for (String line : this.description) {
            MutableText text = Text.empty().formatted(Formatting.GRAY).styled(s -> s.withItalic(false));
            MutableText typeText;

            try {
                Icon.Type type = Icon.Type.valueOf(this.type);
                typeText = Text.literal(type.display).setStyle(Text.empty().getStyle().withColor(type.color).withItalic(false));
            } catch (IllegalArgumentException e) {
                // Show a grayed out version of the name.
                String properCase = CaseMap.Title.toTitle().apply(Locale.ENGLISH, null, this.type.replaceAll("_", " "));
                typeText = Text.literal(properCase).setStyle(Text.empty().getStyle()
                        .withColor(TextColor.fromRgb(0x808080))
                        .withItalic(false));
            }

            if (i == 0) {

                if (this.plural) typeText.append("(s)");
                text.append(typeText);
                if (this.optional) {
                    text.append(Text.literal("*").formatted(Formatting.WHITE));
                }
                text.append(Text.literal(" - ").formatted(Formatting.DARK_GRAY));
                text.append(Utility.textFromString(line).formatted(Formatting.GRAY));
                lore.add(text);
            } else lore.add(Utility.textFromString(line).formatted(Formatting.GRAY));
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

    private void addToLore(ArrayList<Text> lore, String text) {
        lore.add(Utility.textFromString(text));
    }
}