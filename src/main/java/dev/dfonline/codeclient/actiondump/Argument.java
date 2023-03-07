package dev.dfonline.codeclient.actiondump;

import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class Argument {
    public String type;
    public boolean plural;
    public boolean optional;
    public String[] description;
    public String[][] notes;
    public String text;

    private static TextColor GOLD = TextColor.fromFormatting(Formatting.GOLD);
    public enum Type {
        TEXT(TextColor.fromFormatting(Formatting.AQUA), "Text"),
        NUMBER(TextColor.fromFormatting(Formatting.RED), "§cNumber"),
        LOCATION(TextColor.fromFormatting(Formatting.GREEN), "§aLocation"),
        VECTOR(TextColor.fromRgb(0x2AFFAA), "Vector"),
        SOUND(TextColor.fromFormatting(Formatting.BLUE), "Sound"),
        PARTICLE(TextColor.fromRgb(0xAA55FF), "Particle Effect"),
        POTION(TextColor.fromRgb(0xFF557F), "Potion Effect"),
        VARIABLE(TextColor.fromFormatting(Formatting.YELLOW), "Variable"),
        ANY_TYPE(TextColor.fromRgb(0xFFD47F8), "Any Value"),
        ITEM(GOLD, "Item"),
        BLOCK(GOLD, "Block"),
        ENTITY_TYPE(GOLD, "Entity Type"),
        SPAWN_EGG(GOLD, "Spawn Egg"),
        PROJECTILE(GOLD, "Projectile"),
        BLOCK_TAG(TextColor.fromFormatting(Formatting.AQUA), "Block Tag"),
        LIST(TextColor.fromFormatting(Formatting.DARK_GREEN), "List"),
        DICT(TextColor.fromRgb(0xFFD47F8), "Dictionary"),
        NONE(TextColor.fromRgb(0x808080), "None"),
        ;

        public final TextColor color;
        public final String display;
        Type(TextColor color, String display) {
            this.color = color;
            this.display = display;
        }
    }
}