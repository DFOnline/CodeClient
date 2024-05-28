package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

public class Sound extends VarItem {
    private String sound;
    private double pitch;
    private double volume;

    @Override
    public String getId() {
        return "snd";
    }

    @Override
    protected Item getIconItem() {
        return Items.NAUTILUS_SHELL;
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("pitch",1);
        object.addProperty("vol",2);
        object.addProperty("sound","Pling");
        return object;
    }

    public Sound(JsonObject var) {
        super(var);
        this.sound = data.get("sound").getAsString();
        this.pitch = data.get("pitch").getAsDouble();
        this.volume = data.get("vol").getAsDouble();
    }

    public Sound() {
        super();
        this.sound = "Pling";
        this.volume = 2;
        this.pitch = 1;
    }


    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
        this.data.addProperty("sound", sound);
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
        this.data.addProperty("pitch", pitch);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
        this.data.addProperty("vol", volume);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        stack.setCustomName(Text.literal("Sound").setStyle(Style.EMPTY.withItalic(false).withColor(Icon.Type.SOUND.color)));
        Text name;
        try {
            ActionDump db = ActionDump.getActionDump();
            var value = Arrays.stream(db.sounds).filter(gv -> gv.icon.getCleanName().equals(sound)).findFirst();
            if (value.isEmpty()) throw new Exception("");
            name = Text.literal(value.get().icon.name);
        } catch (Exception e) {
            name = Text.literal(sound).setStyle(Style.EMPTY);
        }
        Utility.addLore(stack,
                name,
                Text.empty(),
                Text.empty().append(Text.literal("Pitch: ").formatted(Formatting.GRAY)).append("%.2f".formatted(pitch)),
                Text.empty().append(Text.literal("Volume: ").formatted(Formatting.GRAY)).append("%.2f".formatted(volume)));
        return stack;
    }
}
