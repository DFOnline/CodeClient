package dev.dfonline.codeclient.hypercube.item;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum Scope {
    unsaved(Formatting.GRAY, "GAME", "G"),
    local(Formatting.GREEN, "LOCAL", "L"),
    saved(Formatting.YELLOW, "SAVE", "S");

    public final Formatting color;
    public final String longName;
    public final String shortName;
    Scope(Formatting color, String longName, String shortName) {
        this.color = color;
        this.longName = longName;
        this.shortName = shortName;
    }
}
