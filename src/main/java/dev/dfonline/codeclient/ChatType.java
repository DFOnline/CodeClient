package dev.dfonline.codeclient;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ChatType {
    SUCCESS(Text.literal("»").formatted(Formatting.GREEN, Formatting.BOLD), Formatting.WHITE),
    FAIL(Text.literal("»").formatted(Formatting.DARK_RED, Formatting.BOLD), Formatting.RED),
    INFO(Text.literal("»").formatted(Formatting.BLUE, Formatting.BOLD), Formatting.AQUA);

    private final MutableText prefix;
    private final Formatting trailing;

    ChatType(MutableText prefix, Formatting trailing) {
        this.prefix = prefix;
        this.trailing = trailing;
    }

    public MutableText getText() {
        return this.prefix;
    }

    public Formatting getTrailing() {
        return trailing;
    }
}