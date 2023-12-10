package dev.dfonline.codeclient.hypercube.item;

import dev.dfonline.codeclient.config.Config;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public enum Scope {
    unsaved(TextColor.fromFormatting(Formatting.GRAY), "GAME", "G"),
    local(TextColor.fromFormatting(Formatting.GREEN), "LOCAL", "L"),
    saved(TextColor.fromFormatting(Formatting.YELLOW), "SAVE", "S"),
    line(TextColor.fromRgb(0x55aaff), "LINE", "L");

    public final TextColor color;
    public final String longName;
    public final String shortName;
    Scope(TextColor color, String longName, String shortName) {
        this.color = color;
        this.longName = longName;
        this.shortName = shortName;
    }

    public String getShortName() {
        return this == Scope.line & Config.getConfig().UseIForLineScope ? "I" : shortName;
    }
}
