package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Particle extends VarItem implements Searchable {
    public String particle;
    public String category;
    public ParticleField[] fields;

    @Override
    public List<String> getTerms() {
        return List.of(particle, icon.getCleanName());
    }

    public ItemStack getItem() {
        JsonObject data = new JsonObject();

        data.addProperty("particle", icon.getCleanName());

        JsonObject cluster = new JsonObject();
        cluster.addProperty("amount", 1);
        cluster.addProperty("horizontal", 0.0);
        cluster.addProperty("vertical", 0.0);
        data.add("cluster", cluster);

        JsonObject particleData = new JsonObject();
        List<ParticleField> optionFields = Arrays.stream(fields).toList();
        if (optionFields.contains(ParticleField.Motion)) {
            particleData.addProperty("x", 1);
            particleData.addProperty("y", 0);
            particleData.addProperty("z", 0);
        }
        if (optionFields.contains(ParticleField.MotionVariation)) particleData.addProperty("motionVariation", 100);
        if (optionFields.contains(ParticleField.Color)) particleData.addProperty("rgb", 0xFF0000);
        if (optionFields.contains(ParticleField.ColorVariation)) particleData.addProperty("colorVariation", 0);
        if (optionFields.contains(ParticleField.Size)) particleData.addProperty("size", 1);
        if (optionFields.contains(ParticleField.SizeVariation)) particleData.addProperty("sizeVariation", 0);
        if (optionFields.contains(ParticleField.Material)) particleData.addProperty("material", "STONE");
        data.add("data", particleData);

        ItemStack item = super.getItem("part", data);

        DFItem dfItem = DFItem.of(item);
        List<Text> lore = dfItem.getLore();
        ArrayList<Text> newLore = new ArrayList<>(lore);

        newLore.add(Text.empty());
        newLore.add(Text.literal("Additional Fields:").formatted(Formatting.GRAY));
        if (optionFields.isEmpty()) newLore.add(Text.literal("None").formatted(Formatting.DARK_GRAY));
        else for (ParticleField field : optionFields) {
            if (field != null) newLore.add(Text.literal("• " + field.displayName).formatted(Formatting.WHITE));
            else newLore.add(Text.of("NULL?"));
        }
        dfItem.setLore(newLore);

        return item;
    }

    enum ParticleField {
        @SerializedName("Motion")
        Motion("Motion"),
        @SerializedName("Motion Variation")
        MotionVariation("Motion Variation"),
        @SerializedName("Color")
        Color("Color"),
        @SerializedName("Color Variation")
        ColorVariation("Color Variation"),
        @SerializedName("Material")
        Material("Material"),
        @SerializedName("Size")
        Size("Size"),
        @SerializedName("Size Variation")
        SizeVariation("Size Variation");

        public final String displayName;

        ParticleField(String displayName) {
            this.displayName = displayName;
        }
    }
}
