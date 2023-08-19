package dev.dfonline.codeclient.hypercube.item;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public enum Scope {
    unsaved(TextColor.fromFormatting(Formatting.GRAY), "GAME", "G"),
    local(TextColor.fromFormatting(Formatting.GREEN), "LOCAL", "L"),
    saved(TextColor.fromFormatting(Formatting.YELLOW), "SAVE", "S"),
    line(TextColor.fromRgb(5614335), "LINE", "L");

    public final TextColor color;
    public final String longName;
    public final String shortName;
    Scope(TextColor color, String longName, String shortName) {
        this.color = color;
        this.longName = longName;
        this.shortName = shortName;
    }
}
