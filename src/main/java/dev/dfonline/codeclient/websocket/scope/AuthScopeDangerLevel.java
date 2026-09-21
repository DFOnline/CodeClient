package dev.dfonline.codeclient.websocket.scope;

import net.minecraft.ChatFormatting;

public enum AuthScopeDangerLevel {
    DEFAULT(ChatFormatting.GRAY, "default"),
    SAFE(ChatFormatting.GREEN, "safe"),
    CAUTION(ChatFormatting.YELLOW, "caution"),
    RISKY(ChatFormatting.RED, "risky"),
    DANGEROUS(ChatFormatting.DARK_RED, "dangerous"),
    ;

    public final ChatFormatting color;
    public final String translationKey;
    AuthScopeDangerLevel(ChatFormatting color, String translationKey) {
        this.color = color;
        this.translationKey = translationKey;
    }
}
