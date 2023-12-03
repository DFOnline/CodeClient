package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class Hint extends VarItem {
    public Hint(Item material, JsonObject var) {
        super(material, var);
    }

    private static final Text Parameter = Text.literal("Parameter").fillStyle(Style.EMPTY.withColor(0xAAFFAA));
    private static final Text Line = Text.literal("LINE");
    public static final TextColor HintGreen = TextColor.fromRgb(0xAAFF55);
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
