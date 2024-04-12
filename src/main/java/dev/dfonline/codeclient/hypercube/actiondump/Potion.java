package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.List;

public class Potion extends VarItem implements Searchable {
    public String potion;

    @Override
    public List<String> getTerms() {
        return List.of(potion, icon.getCleanName());
    }

    @Override
    public ItemStack getItem() {
        JsonObject data = new JsonObject();
        data.addProperty("pot", icon.getCleanName());
        data.addProperty("dur", 1000000);
        data.addProperty("amp", 0);
        return super.getItem("pot", data);
    }
}