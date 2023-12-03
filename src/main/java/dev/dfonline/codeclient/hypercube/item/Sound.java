package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class Sound extends VarItem {
    private String sound;
    private double pitch;
    private double volume;

    public Sound(Item material, JsonObject var) {
        super(material, var);
        this.sound  = data.get("sound").getAsString();
        this.pitch  = data.get("pitch").getAsDouble();
        this.volume = data.get("vol"  ).getAsDouble();
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
        this.data.addProperty("sound",sound);
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
        this.data.addProperty("pitch",pitch);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
        this.data.addProperty("vol",volume);
    }
}
