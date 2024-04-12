package dev.dfonline.codeclient.hypercube;

import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public enum Target {
    Selection(Formatting.GREEN, true, true),
    Default(Formatting.GREEN, true, true),
    Killer(Formatting.RED, true, true),
    Damager(Formatting.RED, true, true),
    Shooter(Formatting.YELLOW, true, true),
    Victim(Formatting.BLUE, true, true),
    AllPlayers(Formatting.AQUA, true, false),
    Projectile(Formatting.YELLOW, false, true),
    LastEntity(Formatting.AQUA, false, true);

    public final TextColor color;
    public final boolean onActions;
    public final boolean onGameValue;

    Target(Formatting color, boolean onActions, boolean onGameValue) {
        this.color = TextColor.fromFormatting(color);
        this.onActions = onActions;
        this.onGameValue = onGameValue;
    }
}
