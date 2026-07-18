package dev.dfonline.codeclient.hypercube;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public enum Target {
    Selection(ChatFormatting.GREEN, true, true),
    Default(ChatFormatting.GREEN, true, true),
    Killer(ChatFormatting.RED, true, true),
    Damager(ChatFormatting.RED, true, true),
    Shooter(ChatFormatting.YELLOW, true, true),
    Victim(ChatFormatting.BLUE, true, true),
    AllPlayers(ChatFormatting.AQUA, true, false),
    Projectile(ChatFormatting.YELLOW, false, true),
    LastEntity(ChatFormatting.AQUA, false, true);

    public final TextColor color;
    public final boolean onActions;
    public final boolean onGameValue;

    Target(ChatFormatting color, boolean onActions, boolean onGameValue) {
        this.color = TextColor.fromLegacyFormat(color);
        this.onActions = onActions;
        this.onGameValue = onGameValue;
    }
}
