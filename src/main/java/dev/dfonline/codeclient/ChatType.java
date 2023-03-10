package dev.dfonline.codeclient;

import net.minecraft.util.Formatting;

public enum ChatType {
    SUCCESS("§a§l»", Formatting.WHITE),
    FAIL("§4§l»", Formatting.RED),
    INFO("§9§l»", Formatting.AQUA);

    private final String prefix;
    private final Formatting trailing;

    ChatType(String prefix, Formatting trailing) {
        this.prefix = prefix;
        this.trailing = trailing;
    }

    public String getString() {
        return this.prefix;
    }

    public Formatting getTrailing() {
        return trailing;
    }
}