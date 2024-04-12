package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameValue extends VarItem implements Searchable {
    public String[] aliases;
    public String category;

    @Override
    public List<String> getTerms() {
        ArrayList<String> terms = new ArrayList<>(Arrays.stream(aliases).toList());
        terms.add(icon.name);
        return terms;
    }

    public ItemStack getItem() {
        JsonObject data = new JsonObject();
        data.addProperty("type", icon.getCleanName());
        data.addProperty("target", "Default");
        return super.getItem("g_val", data);
    }
}
