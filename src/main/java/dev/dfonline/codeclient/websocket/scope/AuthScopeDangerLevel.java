package dev.dfonline.codeclient.websocket.scope;

import net.minecraft.util.Formatting;

public enum AuthScopeDangerLevel {
    DEFAULT(Formatting.GRAY, "default"),
    SAFE(Formatting.GREEN, "safe"),
    CAUTION(Formatting.YELLOW, "caution"),
    RISKY(Formatting.RED, "risky"),
    DANGEROUS(Formatting.DARK_RED, "dangerous"),
    ;

    public final Formatting color;
    public final String translationKey;
    AuthScopeDangerLevel(Formatting color, String translationKey) {
        this.color = color;
        this.translationKey = translationKey;
    }
}
