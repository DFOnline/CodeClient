package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.List;

public class Sound extends VarItem implements Searchable {
    public String sound;
    public String soundId;
    public Variant[] variants;

    @Override
    public List<String> getTerms() {
        return List.of(sound, icon.name);
    }

    public ItemStack getItem() {
        JsonObject data = new JsonObject();
        data.addProperty("sound", icon.getCleanName());
        data.addProperty("pitch", 1);
        data.addProperty("vol", 2);
        return super.getItem("snd", data);
    }

    public record Variant(String id, String name, long seed) {
    }

    public Variant getVariantFromName(String id) {
        if (variants == null) {
            return null;
        }
        for (Variant variant : variants) {
            if (variant.id.equals(id)) {
                return variant;
            }
        }
        return null;
    }
}
