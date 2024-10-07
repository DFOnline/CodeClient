package dev.dfonline.codeclient.dev.highlighter;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.hypercube.HypercubeCommands;
import dev.dfonline.codeclient.hypercube.HypercubeMinimessage;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static net.kyori.adventure.platform.fabric.FabricAudiences.nonWrappingSerializer;

public class ExpressionHighlighter extends Feature {
    private final Set<String> CODES = Set.of(
            "default",
            "selected",
            "uuid",
            "var",
            "math",
            "damager",
            "killer",
            "shooter",
            "victim",
            "projectile",
            "random",
            "round",
            "index",
            "entry"
    );

    private final List<TextColor> COLORS = List.of(
            TextColor.fromRgb(0xffd600)
    );


    @Override
    public boolean enabled() {
        return CodeClient.location instanceof Dev; // todo: config
    }

    public record HighlightedExpression(OrderedText text, @Nullable OrderedText preview) {

    }

    private String cachedInput = "";
    private boolean cachedParseMinimessage = true;
    private HighlightedExpression cachedHighlight = new HighlightedExpression(OrderedText.empty(), null);

    private final MiniMessage formatter = HypercubeMinimessage.FORMATTER;
    private final MiniMessageHighlighter highlighter = new MiniMessageHighlighter();

    // creates an expression based on text input
    public HighlightedExpression format(String input) {
        OrderedText text = convert(highlighter.highlight(input));
        OrderedText preview = convert(formatter.deserialize(input));

        return new HighlightedExpression(text, preview);
    }

    // draws the preview above the chat bar
    public void draw(DrawContext context, int mouseX, int mouseY, OrderedText input /* todo take out of mixin */) {
        TextRenderer renderer = CodeClient.MC.textRenderer;

        var screen = CodeClient.MC.currentScreen;
        if (screen == null) return;

        int y = screen.height - 25;
        context.drawTextWithShadow(renderer, input, 4, y,0xffffff);
    }

    private OrderedText convert(Component component) {
        return nonWrappingSerializer().serialize(component).asOrderedText();
    }


    private class EmptyInsertion implements Inserting {
        @Override
        public @NotNull Component value() { return Component.empty(); }
    }

    private enum CommandType {
        NUMBER(HypercubeCommands.NUMBER, true, false),
        STRING(HypercubeCommands.STRING, true, false),
        TEXT(HypercubeCommands.TEXT, true),
        VARIABLE(HypercubeCommands.VARIABLE, true, false),
        ITEM_NAME(HypercubeCommands.ITEM_NAME),
        ITEM_LORE_ADD(HypercubeCommands.ITEM_LORE_ADD),
        ITEM_LORE_SET(HypercubeCommands.ITEM_LORE_SET, 1),
        ITEM_LORE_INSERT(HypercubeCommands.ITEM_LORE_INSERT, 1),
        PLOT_NAME(HypercubeCommands.PLOT_NAME),
        RELORE(HypercubeCommands.RELORE);

        final Pattern regex;
        final int argumentIndex;
        final boolean hasCount;
        final boolean parseMinimessage;

        CommandType(Pattern regex) {
            this(regex, 0);
        }

        CommandType(Pattern regex, int argumentIndex) {
            this(regex, argumentIndex, false, true);
        }

        CommandType(Pattern regex, boolean hasCount) {
            this(regex, 0, hasCount, true);
        }

        CommandType(Pattern regex, boolean hasCount, boolean parseMinimessage) {
            this(regex, 0, hasCount, parseMinimessage);
        }

        CommandType(Pattern regex, int argumentIndex, boolean hasCount, boolean parseMinimessage) {
            this.regex = regex;
            this.argumentIndex = argumentIndex;
            this.hasCount = hasCount;
            this.parseMinimessage = parseMinimessage;
        }
    }
}