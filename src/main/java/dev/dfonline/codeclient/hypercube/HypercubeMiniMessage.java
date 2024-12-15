package dev.dfonline.codeclient.hypercube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class HypercubeMiniMessage {
    public static final MiniMessage MM = MiniMessage.builder().tags(Tags.ALL).build();

    public static final TagResolver SPACE_TAG = Tags.SPACE;
    public static final TagResolver NEWLINE_TAG = Tags.NEWLINE;

    private static class Tags {
        private static final int MAX_REPETITION_COUNT = 32;

        // todo: remove when adventure is >5.15.0 (minecraft 1.21)
        private static final TagResolver STANDARD = StandardTags.defaults();

        static final TagResolver SPACE = repetitionTagResolver("space", " ");
        static final TagResolver NEWLINE = repetitionTagResolver("newline", "\n");

        static final TagResolver ALL = TagResolver.resolver(STANDARD, SPACE, NEWLINE);

        private static TagResolver repetitionTagResolver(String name, String literal) {
            return TagResolver.resolver(name, (arguments, context) -> {
                int count;
                if (arguments.hasNext()) {
                    count = arguments.pop().asInt().orElseThrow(() -> {
                        context.newException("Count must be a number");
                        return null;
                    });
                } else count = 1;

                String repeated = literal.repeat(Math.min(count, MAX_REPETITION_COUNT));
                return Tag.selfClosingInserting(Component.text(repeated));
            });
        }
    }
}