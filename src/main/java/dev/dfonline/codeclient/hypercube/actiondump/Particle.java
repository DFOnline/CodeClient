package dev.dfonline.codeclient.hypercube.actiondump;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

import static dev.dfonline.codeclient.Utility.nbtify;

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

        NbtCompound display = item.getSubNbt("display");
        NbtList Lore = (NbtList) display.get("Lore");
        Lore.add(nbtify(Text.literal("")));
        Lore.add(nbtify(Text.literal("Additional Fields:").formatted(Formatting.GRAY)));
        if (optionFields.size() == 0) Lore.add(nbtify(Text.literal("None").formatted(Formatting.DARK_GRAY)));
        else for (ParticleField field : optionFields) {
            if (field != null) Lore.add(nbtify(Text.literal("• " + field.displayName).formatted(Formatting.WHITE)));
            else Lore.add(nbtify(Text.of("NULL?")));
        }
        display.put("Lore", Lore);
        item.setSubNbt("display", display);

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
