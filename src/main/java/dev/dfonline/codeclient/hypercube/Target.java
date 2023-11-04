package dev.dfonline.codeclient.hypercube;

import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public enum Target {
    Selection(Formatting.GREEN),
    Default(Formatting.GREEN),
    Killer(Formatting.RED),
    Damager(Formatting.RED),
    Shooter(Formatting.YELLOW),
    Victim(Formatting.BLUE),
    AllPlayers(Formatting.AQUA);

    public final TextColor color;
    Target(Formatting color) {
        this.color = TextColor.fromFormatting(color);
    }
}
