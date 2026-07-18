package dev.dfonline.codeclient;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum ChatType {
    SUCCESS(Component.literal("»").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)),
    FAIL(Component.literal("»").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)),
    INFO(Component.literal("»").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

    private final MutableComponent prefix;

    ChatType(MutableComponent prefix) {
        this.prefix = prefix;
    }

    public MutableComponent getText() {
        return this.prefix;
    }
}