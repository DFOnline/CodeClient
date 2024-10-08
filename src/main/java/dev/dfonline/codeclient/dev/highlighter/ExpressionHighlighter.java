package dev.dfonline.codeclient.dev.highlighter;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.hypercube.HypercubeCommands;
import dev.dfonline.codeclient.hypercube.HypercubeMinimessage;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import dev.dfonline.codeclient.location.Dev;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
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
    private int cachedPosition = 0;
    private HighlightedExpression cachedHighlight = new HighlightedExpression(OrderedText.empty(), null);

    private final MiniMessage formatter = HypercubeMinimessage.FORMATTER;
    private final MiniMessageHighlighter highlighter = new MiniMessageHighlighter();

    // creates an expression based on text input
    public HighlightedExpression format(String input, String partial, IntegerRange range) {
        if (Objects.equals(input, cachedInput) && cachedPosition == range.getMinimum()) {
            return cachedHighlight;
        }

        cachedPosition = range.getMinimum();
        cachedInput = input;

        // parse commands
        if (input.startsWith("/")) {
            for (CommandType command : CommandType.values()) {
                var matcher = command.regex.matcher(input);
                if (!matcher.find(1)) {
                    continue;
                }
                cachedHighlight = formatCommand(input, command, matcher.end(), range);
                return cachedHighlight;
            }
            cachedHighlight = null;
            return null;
        }

        var player = CodeClient.MC.player;
        if (player == null) return null;
        var item = player.getMainHandStack();
        try {
            VarItem varItem = VarItems.parse(item);
            Component text;

            switch (varItem.getId()) {
                case "num", "var", "txt" -> text = highlighter.highlight(input, false);
                case "comp" -> text = highlighter.highlight(input, true);
                default -> {
                    return null;
                }
            }

            Component preview = formatter.deserialize(input);

            cachedHighlight = new HighlightedExpression(subSequence(convert(text), range), convert(preview));
            return cachedHighlight;
        } catch (Exception ignored) {
            cachedHighlight = null;
            return null;
        }
    }

    private HighlightedExpression formatCommand(String input, CommandType command, int index, IntegerRange range) {
        int start = index;
        int end = input.length();

        if (start > end) return null;


        if (command.argumentIndex > 0) {
            var pattern = Pattern.compile("^(\\S*\\s+){"+command.argumentIndex+"}");
            var matcher = pattern.matcher(input);
            if (matcher.find(start)) {
                start = matcher.end();
            }
            if (start > end) return null;
        }

        if (command.hasCount) {
            var pattern = Pattern.compile("\\s\\d+$");
            var matcher = pattern.matcher(input);
            if (matcher.find(start)) {
                end = matcher.start();
            }
        }

        // edit box
        Component highlighted = highlighter.highlight(
                input.substring(start, end),
                command.parseMinimessage
        );

        Component combined = Component.text(input.substring(0, start)).color(NamedTextColor.GRAY).append(highlighted);

        if (end < input.length()) {
            combined = combined.append(Component.text(input.substring(end)).color(NamedTextColor.LIGHT_PURPLE));
        }

        // preview text
        Component formatted = formatter.deserialize(input.substring(start, end));

//        return new HighlightedExpression(splitComponent(combined, cursor), convert(formatted));
        return new HighlightedExpression(subSequence(convert(combined), range), convert(formatted));
    }

    private OrderedText splitComponent(Component component, int cursor) {
        String raw = PlainTextComponentSerializer.plainText().serialize(component);

        Pattern half = Pattern.compile("^.{"+(cursor-1)+"}");
        Matcher matcher = half.matcher(raw);

        if (!matcher.matches()) return OrderedText.concat(convert(component));

        Pattern first = Pattern.compile("^"+matcher.group());
        Pattern last = Pattern.compile(matcher.replaceAll("")+"$");

        Component firstComponent = component.replaceText(builder -> {
            builder.replacement("");
            builder.match(last);
        });
        Component secondComponent = component.replaceText(builder -> {
            builder.replacement("");
            builder.match(first);
        });

        return OrderedText.concat(convert(firstComponent), convert(secondComponent));
    }

    /** takes a given substring of a component.
     * @param start inclusive
     * @param end exclusive
     * @return a component containing what's between the start index and end index.
     */
    private Component substr(Component component, int start, int end) {
        String raw = PlainTextComponentSerializer.plainText().serialize(component);
        String split = raw.substring(start, end);

        // this variable reveals what needs to be replaced in the component.
        var replace = raw.split(split);

        if (split.equals(raw)) return component;

        return component.replaceText(builder -> {
            for (String section : replace) {
                builder.matchLiteral(section);
            }
            builder.replacement("");
        });
    }

    /** takes a given substring of a component.
     * @param start inclusive
     * @return a component containing what's after the start index
     */
    private Component substr(Component component, int start) {
        String raw = PlainTextComponentSerializer.plainText().serialize(component);
        return substr(component, start, raw.length()+1);
    }

    private OrderedText subSequence(OrderedText original, int start, int end) {
        return visitor -> acceptWithAbsoluteIndex(original, (index, style, codePoint) -> {
            if (index >= start && index < end) {
                return visitor.accept(index - start, style, codePoint);
            }
            return true;
        });
    }

    private OrderedText subSequence(OrderedText original, IntegerRange range) {
        int start = range.getMinimum();
        int end = range.getMaximum();

        return subSequence(original, start, end);
    }

    public boolean acceptWithAbsoluteIndex(OrderedText original, CharacterVisitor visitor) {
        AtomicInteger index = new AtomicInteger();
        return original.accept((ignored, style, codePoint) -> {
            final boolean shouldContinue = visitor.accept(index.getAndIncrement(), style, codePoint);
            if (Character.isHighSurrogate(Character.toString(codePoint).charAt(0))) {
                index.getAndIncrement();
            };
            return shouldContinue;
        });
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