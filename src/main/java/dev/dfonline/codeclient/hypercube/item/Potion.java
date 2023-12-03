package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class Potion extends VarItem {
    private String potion;
    private int duration;
    private int amplifier;

    public Potion(Item material, JsonObject var) {
        super(material, var);
        this.potion = data.get("pot").getAsString();
        this.duration = data.get("dur").getAsInt();
        this.amplifier = data.get("amp").getAsInt();
    }

    public String getPotion() {
        return potion;
    }
    public void setPotion(String potion) {
        data.addProperty("pot",potion);
        this.potion = potion;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        data.addProperty("dur",duration);
        this.duration = duration;
    }
    public int getAmplifier() {
        return amplifier;
    }
    public void setAmplifier(int amplifier) {
        data.addProperty("amp",amplifier);
        this.amplifier = amplifier;
    }
}
