package dev.dfonline.codeclient.dev.highlighter;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.HypercubeCommands;
import dev.dfonline.codeclient.hypercube.HypercubeMiniMessage;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.dfonline.codeclient.Utility.componentToOrderedText;

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

    // palette copied from recode, maybe there's a better combination?
    private final List<TextColor> COLORS = List.of(
            TextColor.fromRgb(0xffd42a),
            TextColor.fromRgb(0x33ff00),
            TextColor.fromRgb(0x00ffe0),
            TextColor.fromRgb(0x5e77f7),
            TextColor.fromRgb(0xca64fa),
            TextColor.fromRgb(0xff4242)
    );

    private final TextColor ERROR_COLOR = TextColor.fromFormatting(Formatting.RED);

    @Override
    public boolean enabled() {
        return Config.getConfig().HighlighterEnabled;
    }

    public record HighlightedExpression(OrderedText text, @Nullable OrderedText preview) {}

    private String cachedInput = "";
    private int cachedPosition = 0;
    private HighlightedExpression cachedHighlight = new HighlightedExpression(OrderedText.empty(), null);

    private final MiniMessage formatter = HypercubeMiniMessage.MM;
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
                } else if (matcher.start() > 1) {
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

            boolean showPreview = false;

            switch (varItem.getId()) {
                case "num", "var", "txt" -> {
                        if (Config.getConfig().HighlightExpressions) text = highlightExpressions(input, true);
                        else text = Component.text(input);
                }
                case "comp" -> {
                    if (Config.getConfig().HighlightMiniMessage) text = highlighter.highlight(input);
                    else text = Component.text(input);

                    showPreview = true;

                    if (Config.getConfig().HighlightExpressions) text = highlightExpressions(text);
                }
                default -> {
                    cachedHighlight = null;
                    return null;
                }
            }

            Component preview = Component.empty();
            if (showPreview) preview = formatter.deserialize(input);

            cachedHighlight = new HighlightedExpression(subSequence(componentToOrderedText(text), range), componentToOrderedText(preview));
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
        Component highlighted;
        if (command.parseMinimessage) {
            if (Config.getConfig().HighlightMiniMessage) highlighted = highlighter.highlight(input.substring(start,end));
            else highlighted = Component.text(input.substring(start,end));

            if (Config.getConfig().HighlightExpressions) highlighted = highlightExpressions(highlighted);
        } else {
            if (Config.getConfig().HighlightExpressions) highlighted = highlightExpressions(input.substring(start,end), true);
            else highlighted = Component.text(input.substring(start,end));
        }

        Component combined = Component.text(input.substring(0, start)).color(NamedTextColor.GRAY).append(highlighted);

        if (end < input.length()) {
            combined = combined.append(Component.text(input.substring(end)).color(NamedTextColor.LIGHT_PURPLE));
        }

        // preview text
        Component formatted = Component.empty();
        if (command.parseMinimessage) {
            formatted = formatter.deserialize(input.substring(start, end));
        }

//        return new HighlightedExpression(splitComponent(combined, cursor), convert(formatted));
        return new HighlightedExpression(subSequence(componentToOrderedText(combined), range), componentToOrderedText(formatted));
    }

    private Component highlightExpressions(Component component) {
        String raw = formatter.serialize(component);

        return highlightExpressions(raw, false);
    }

    private Component highlightExpressions(String input, boolean escapeTags) {
        String raw = escapeTags ? formatter.escapeTags(input) : input;

        StringBuilder sb = new StringBuilder(raw.length());

        Pattern pattern = Pattern.compile("(%[a-zA-Z]+\\(?)|\\)|$");
        Matcher matcher = pattern.matcher(raw);

        int depth = 0;
        int start = 0;

        boolean invalid = false;

        while (matcher.find()) {
            String value = matcher.group();
            if (Objects.equals(value, ")")) {
                if (start != matcher.start()) {
                    String prev = raw.substring(start, matcher.start());
                    sb.append(prev);
                }

                if (depth <= 0) {
                    sb.append(value);
                    start = matcher.end();
                    continue;
                }

                var color = getColor(depth);
                depth--;

                String style;
                if (!invalid) {
                    style = color.getHexCode();
                    sb.append(String.format("</color:%s>", style));
                    style = getColor(depth).getHexCode();
                } else {
                    style = ERROR_COLOR.getHexCode();
                    sb.append(String.format("</color:%s>", style));
                }
                sb.append(value);

                depth--;
                sb.append(String.format("</color:%s>", style));
            } else {
                depth++;

                var color = getColor(depth);
                if ((value.length() > 1 && !CODES.contains(value.replace("%", "").replace("(", "")) && depth <= 1) || invalid) {
                    color = ERROR_COLOR;
                    invalid = true;
                }

                if (start != matcher.start()) {
                    String prev = raw.substring(start, matcher.start());
                    sb.append(prev);
                }

                String style = color.getHexCode();
                sb.append(String.format("<color:%s>", style));
                sb.append(value);

                if (value.endsWith("(")) {
                    depth++;
                    if (!invalid) {
                        style = getColor(depth).getHexCode();
                    }
                    sb.append(String.format("<color:%s>", style));
                } else if (depth > 0){
                    depth--;

                    sb.append(String.format("</color:%s>", style));
                }
            }
            start = matcher.end();
        }

        return formatter.deserialize(sb.toString());
    }

    private TextColor getColor(int depth) {
        if (depth < 1) return getColor(COLORS.size() - depth);
        try {
            return COLORS.get(depth - 1);
        } catch (Exception e) {
            return getColor(depth - COLORS.size());
        }
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
            }
            return shouldContinue;
        });
    }

    // draws the preview above the chat bar
    public void draw(DrawContext context, int mouseX, int mouseY, OrderedText input) {
        TextRenderer renderer = CodeClient.MC.textRenderer;

        var screen = CodeClient.MC.currentScreen;
        if (screen == null) return;

        int y = screen.height - 25;
        context.drawTextWithShadow(renderer, input, 4, y,0xffffff);
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