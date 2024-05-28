package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class Hint extends VarItem {
    public static final TextColor HintGreen = TextColor.fromRgb(0xAAFF55);
    private static final Text Parameter = Text.literal("Parameter").fillStyle(Style.EMPTY.withColor(0xAAFFAA));
    private static final Text Line = Text.literal("LINE");

    @Override
    public String getId() {
        return "hint";
    }

    @Override
    protected Item getIconItem() {
        return Items.KNOWLEDGE_BOOK;
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("hint","function");
        return object;
    }

    public Hint(JsonObject var) {
        super(var);
    }

    public Hint() {
        super();
    }

    public enum HintType {
        function("Function Parameters",
                Text.empty().formatted(Formatting.GRAY)
                        .append("Put").append(Parameter)
                        .append(" items in this chest to set \n")
                        .append(Text.literal("\n"))
                        .append("In this code line, use the ").append(Line).append(" variable \n")
                        .append("scope to access the parameter values. \n")
                        .append("\n")
                        .append(Line).append(" variable items for each parameter \n")
                        .append("can be obtained by right clicking on a \n")
                        .append(Parameter).append(" item in this chest.")
        );

        public final String name;
        public final Text text;

        HintType(String name, Text text) {
            this.name = name;
            this.text = text;
        }
    }
}
