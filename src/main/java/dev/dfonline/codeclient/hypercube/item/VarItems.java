package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VarItems {
    @Nullable
    public static VarItem parse(ItemStack item) {
        Item material = item.getItem();
        JsonObject var;
        try {
            var = VarItem.prefetch(item);
            return switch (var.get("id").getAsString()) {
                case "txt", "comp" -> new NamedItem(material, var);
                case "num" -> new Number(material, var);
                case "var" -> new Variable(material, var);
                case "loc" -> new Location(material, var);
                case "vec" -> new Vector(material, var);
                case "pot" -> new Potion(material, var);
                case "snd" -> new Sound(material, var);
                case "g_val" -> new GameValue(material, var);
                case "pn_el" -> new Parameter(material, var); // Short for `pattern_element`. Parameters, each bullet in a actions Chest Parameters is a pattern internally. Apparently.
                case "bl_tag" -> new BlockTag(material, var);
                case "hint" -> new Hint(material, var); // we're so funny
                default -> null;
            };
        }
        catch (Exception e) {
            return null;
        }
    }
}
