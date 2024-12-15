package dev.dfonline.codeclient.hypercube;

import java.util.regex.Pattern;

/**
 * Hypercube command pattern matchers which include their aliases as well as their trailing space.
 */
public class HypercubeCommands {
    private static final Pattern ITEM = argument("item", "i");
    private static final Pattern LORE = argument("lore", "l");

    public static final Pattern ITEM_NAME = multi(
            command(ITEM, argument("name", "n")),
            command(argument("rename"))
    );

    public static final Pattern ITEM_LORE_ADD = multi(
            command(ITEM, LORE, argument("add", "a")),
            command(LORE, argument("add", "a")),
            command(argument("addlore", "ila"))
    );
    public static final Pattern ITEM_LORE_SET = multi(
            command(ITEM, LORE, argument("set", "s")),
            command(LORE, argument("set", "s")),
            command(argument("setlore", "ils"))
    );
    public static final Pattern ITEM_LORE_INSERT = multi(
            command(ITEM, LORE, argument("insert", "i")),
            command(LORE, argument("insert", "i"))
    );

    public static final Pattern PLOT_NAME = command(argument("plot", "p"), argument("name"));

    public static final Pattern NUMBER = command("number", "num");
    public static final Pattern STRING = command("string", "str");
    public static final Pattern TEXT = command("styledtext", "text", "stxt", "txt");
    public static final Pattern VARIABLE = command("variable", "var");

    public static final Pattern RELORE = command("relore");


    // utility functions:
    private static Pattern argument(String... strings) {
        var builder = new StringBuilder();

        boolean separator = false;
        for (String string : strings) {
            if (separator) builder.append("|");
            else separator = true;
            builder.append("(").append(string).append(")");
        }

        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }

    private static Pattern command(Pattern... arguments) {
        var builder = new StringBuilder();

        boolean separator = false;
        for (Pattern argument : arguments) {
            if (separator) builder.append(" ");
            else separator = true;
            builder.append("(").append(argument.pattern()).append(")");
        }
        builder.append(" ");

        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }

    private static Pattern command(String... strings) {
        return Pattern.compile(argument(strings).pattern()+" ");
    }

    private static Pattern multi(Pattern... commands) {
        var builder = new StringBuilder();

        boolean separator = false;
        for (Pattern command : commands) {
            if (separator) builder.append("|");
            else separator = true;
            builder.append("(").append(command.pattern()).append(")");
        }

        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }
}
