package dev.dfonline.codeclient;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ChatType {
    SUCCESS(Text.literal("»").formatted(Formatting.GREEN, Formatting.BOLD)),
    FAIL(Text.literal("»").formatted(Formatting.RED, Formatting.BOLD)),
    INFO(Text.literal("»").formatted(Formatting.AQUA, Formatting.BOLD));

    private final MutableText prefix;

    ChatType(MutableText prefix) {
        this.prefix = prefix;
    }

    public MutableText getText() {
        return this.prefix;
    }
}