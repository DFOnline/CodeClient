package dev.dfonline.codeclient.dev.highlighter;

import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.HypercubeMiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.TokenType;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.tag.Inserting;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// /
//  * Parses MiniMessage input, but leaves the tags in the message for formatting in the edit box.
//  /
@SuppressWarnings("UnstableApiUsage")
public class MiniMessageHighlighter {
    public MiniMessage HIGHLIGHTER = MiniMessage.builder().tags(TagResolver.resolver(
            new ShownTagResolver()
    )).build();
    public MiniMessage PARSER = MiniMessage.builder().tags(TagResolver.resolver(
            new ShownTagResolver(),
            HypercubeMiniMessage.NEWLINE_TAG,
            HypercubeMiniMessage.SPACE_TAG
    )).build();

    private final String RESET_TAG = "<reset>";
    private String getTagStyle() {
        return TextColor.color(Config.getConfig().MiniMessageTagColor).asHexString();
    }

    public Component highlight(String input) {

        // handle "resets"
        var resets = input.split("(?i)"+RESET_TAG);
        if (resets.length > 1 || input.toLowerCase().endsWith(RESET_TAG)) {
            Component value = Component.empty();

            Component reset = PARSER.deserialize(String.format("<%s>", getTagStyle()) + HIGHLIGHTER.escapeTags(RESET_TAG) + String.format("</%s>", getTagStyle()));

            for (String partial : resets) {
                value = value.append(highlight(partial))
                        .append(reset);
            }
            return value;
        }

        Node.Root root = PARSER.deserializeToTree(input);
        StringBuilder newInput = new StringBuilder(input.length());

        handle(root, root.input(), newInput, new AtomicInteger(), new ArrayList<>());
        return HIGHLIGHTER.deserialize(newInput.toString());
    }

    @SuppressWarnings("UnstableApiUsage")
    private void handle(Node node, String full, StringBuilder sb, AtomicInteger index, ArrayList<TagNode> activeTags) {
        String style = getTagStyle();

        if (node instanceof TagNode tagNode) {
            String tagString = getTokenString(tagNode.token(), full);

            index.addAndGet(tagString.length());

            appendEscapedTag(sb, tagString, style, activeTags, full);

            if (isActiveTag(tagNode)) {
                activeTags.add(tagNode);
            }

            // prevent "space" and "newline" tags from being added extra as they dont get parsed in the chatbox.
            if (!(tagString.contains("space") || tagString.contains("newline"))) sb.append(tagString);
        } else if (node instanceof ValueNode valueNode) {
            String value = getTokenString(valueNode.token(), full);

            index.addAndGet(value.length());

            sb.append(PARSER.escapeTags(escapeBackslashes(value)));
        }

        for (Node child : node.children()) {
            handle(child, full, sb, index, activeTags);
        }

        if (node instanceof TagNode tagNode) {
            String tagName = tagNode.name();
            String closing = String.format("</%s>", tagName);

            if (full.startsWith(closing, index.get())) {
                if (isActiveTag(tagNode)) {
                    activeTags.remove(tagNode);
                }

                index.addAndGet(closing.length());
                sb.append(closing);
                appendEscapedTag(sb, closing, style, activeTags, full);
            }
        }
    }

    private void appendEscapedTag(StringBuilder sb, String tag, String style, ArrayList<TagNode> activeTags, String full) {
        for (int i = activeTags.size() - 1; i >= 0; i--) {
            sb.append("</").append(activeTags.get(i).name()).append(">");
        }

        interpolate(sb, "<", style, ">", HIGHLIGHTER.escapeTags(escapeBackslashes(tag)), "</", style, ">");

        for (TagNode activeTag : activeTags) {
            sb.append(getTokenString(activeTag.token(), full));
        }
    }

    private boolean isActiveTag(TagNode tagNode) {
        if (tagNode.token().type() == TokenType.OPEN_CLOSE_TAG) return false;

        Tag tag = tagNode.tag();
        return tag instanceof Modifying
                || tag instanceof Inserting inserting && inserting.allowsChildren();
    }

    private String escapeBackslashes(String value) {
        return value.replace("\\", "\\\\");
    }

    private void interpolate(StringBuilder sb, String... substrings) {
        for (String substring : substrings) {
            sb.append(substring);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private String getTokenString(Token token, String root) {
        int start = token.startIndex();
        int end = token.endIndex();
        return root.substring(start, end);
    }

    private static class ShownTagResolver implements TagResolver {
        private final TagResolver standard = TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations(TextDecoration.BOLD),
                StandardTags.decorations(TextDecoration.ITALIC),
                StandardTags.decorations(TextDecoration.UNDERLINED),
                StandardTags.decorations(TextDecoration.STRIKETHROUGH),
                StandardTags.gradient(),
                StandardTags.pride(),
                StandardTags.rainbow(),
                StandardTags.reset(),
                StandardTags.sequentialHead(),
                StandardTags.shadowColor(),
                StandardTags.sprite()
        );

        @Override
        public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
            Tag tag = standard.resolve(name, arguments, ctx);
            return tag;
        }

        @Override
        public boolean has(@NotNull String name) {
            return standard.has(name);
        }
    }

}
