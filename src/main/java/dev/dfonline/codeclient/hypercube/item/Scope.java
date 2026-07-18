package dev.dfonline.codeclient.hypercube.item;

import dev.dfonline.codeclient.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public enum Scope {
    unsaved(TextColor.fromLegacyFormat(ChatFormatting.GRAY), "GAME", "G", "g"),
    local(TextColor.fromLegacyFormat(ChatFormatting.GREEN), "LOCAL", "L", "l"),
    saved(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), "SAVE", "S", "s"),
    line(TextColor.fromRgb(0x55aaff), "LINE", "L", "i");

    public final TextColor color;
    public final String longName;
    public final String shortName;
    public final String tag;

    Scope(TextColor color, String longName, String shortName, String tag) {
        this.color = color;
        this.longName = longName;
        this.shortName = shortName;
        this.tag = tag;
    }

    public String getShortName() {
        return this == Scope.line & Config.getConfig().UseIForLineScope ? "I" : shortName;
    }
}
