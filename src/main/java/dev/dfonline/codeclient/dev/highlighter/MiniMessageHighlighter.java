package dev.dfonline.codeclient.dev.highlighter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode;
import net.kyori.adventure.text.minimessage.internal.parser.node.TagPart;
import net.kyori.adventure.text.minimessage.internal.parser.node.ValueNode;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.minimessage.tree.Node;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Parses MiniMessage input, but leaves the tags in the message for formatting in the edit box.
 */
public class MiniMessageHighlighter {
    public MiniMessage HIGHLIGHTER = MiniMessage.builder().tags(new ShownTagResolver()).build();


    public Component highlight(String input) {
        Node.Root root = HIGHLIGHTER.deserializeToTree(input);

        System.out.println(root.input());

        int index = 0;

        handle(root);



        return Component.text(input).color(NamedTextColor.AQUA);
    }



    @SuppressWarnings("UnstableApiUsage")
    private String handle(Node node) {
        String style;
        if (node instanceof TagNode tagNode) {
            System.out.println(tagNode.tag() + " | " + tagNode.name());
            for (TagPart part : tagNode.parts()) {
                System.out.println(part.token() + " : " + part.value());
            }

            style = "gray";
        }
        if (node instanceof ValueNode valueNode) {
            System.out.println(valueNode.value() + " | " + valueNode.token());
            style = "gray";
        }

        for (Node child : node.children()) {
            System.out.println("- new child");
            handle(child);
            System.out.println("- end of child");
        }

        return null;
    }


    private class ShownTagResolver implements TagResolver {
        private final TagResolver standard = TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations(TextDecoration.BOLD),
                StandardTags.decorations(TextDecoration.ITALIC),
                StandardTags.decorations(TextDecoration.UNDERLINED),
                StandardTags.decorations(TextDecoration.STRIKETHROUGH),
                StandardTags.reset(),
                StandardTags.gradient(),
                StandardTags.rainbow()
        );

        @Override
        public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
            Tag tag = standard.resolve(name, arguments, ctx);
            if (tag != null) {
                return tag;
            }
            return null;
        }

        @Override
        public boolean has(@NotNull String name) {
            return standard.has(name);
        }
    }

}
