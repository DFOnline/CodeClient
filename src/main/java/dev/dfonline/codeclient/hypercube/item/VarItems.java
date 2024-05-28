package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VarItems {
    @Nullable
    public static VarItem parse(ItemStack item) {
        JsonObject var;
        try {
            var = VarItem.prefetch(item);
            return switch (var.get("id").getAsString()) {
                case "txt" -> new Text(var);
                case "comp" -> new Component(var);
                case "num" -> new Number(var);
                case "var" -> new Variable(var);
                case "loc" -> new Location(var);
                case "vec" -> new Vector(var);
                case "pot" -> new Potion(var);
                case "snd" -> new Sound(var);
                case "g_val" -> new GameValue(var);
                case "pn_el" ->
                        new Parameter(var); // Short for `pattern_element`. Parameters, each bullet in a actions Chest Parameters is a pattern internally. Apparently.
                case "bl_tag" -> new BlockTag(var);
                case "hint" -> new Hint(var); // we're so funny
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
