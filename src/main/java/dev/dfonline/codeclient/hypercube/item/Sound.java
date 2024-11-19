package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Sound extends VarItem {
    private String sound;
    private double pitch;
    private double volume;

    public static final Map<String, Double> Notes;
    public static final Map<Double, String> Pitches;

    static {
        var notes = new HashMap<String, Double>();

        notes.put("F#0",0.5d);
        notes.put( "G0",0.529732d);
        notes.put("G#0",0.561231d);
        notes.put( "A1",0.594604d);
        notes.put("A#1",0.629961d);
        notes.put( "B1", 0.66742d);
        notes.put( "C1",0.707107d);
        notes.put("C#1",0.749154d);
        notes.put( "D1",0.793701d);
        notes.put("D#1",0.840896d);
        notes.put( "E1",0.890899d);
        notes.put( "F1",0.943874d);
        notes.put("F#1",1d);
        notes.put( "G1",1.059463d);
        notes.put("G#1",1.122462d);
        notes.put( "A2",1.189207d);
        notes.put("A#2",1.259921d);
        notes.put( "B2", 1.33484d);
        notes.put( "C2",1.414214d);
        notes.put("C#2",1.498307d);
        notes.put( "D2",1.587401d);
        notes.put("D#2",1.681793d);
        notes.put( "E2",1.781797d);
        notes.put( "F2",1.887749d);
        notes.put("F#2",2d);

        var pitches = new HashMap<Double, String>();
        for(var entry : notes.entrySet()) pitches.put(entry.getValue(), entry.getKey());

        Notes = notes;
        Pitches = pitches;
    }

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

    public Optional<dev.dfonline.codeclient.hypercube.actiondump.Sound> getSoundId() {
        try {
            return Arrays.stream(ActionDump.getActionDump().sounds).filter(sound -> Objects.equals(sound.icon.getCleanName(), this.sound)).findFirst();
        } catch (Exception ignored) {

        }
        return Optional.empty();
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

    public @Nullable String getNote() {
        return Pitches.get(getPitch());
    }
    public void setNote(String note) {
        var sharp = note.contains("#");
        var octave = note.replaceAll("[^012]","");
        var key = note.toUpperCase().replaceAll("[^A-G]","");
        var pitch = Notes.get(sharp ? octave + key + "#" : octave + key);
        if(pitch != null) this.pitch = pitch;
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
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Text.literal("Sound").setStyle(Style.EMPTY.withItalic(false).withColor(Icon.Type.SOUND.color)));
        Text name;
        try {
            ActionDump db = ActionDump.getActionDump();
            var value = Arrays.stream(db.sounds).filter(gv -> gv.icon.getCleanName().equals(sound)).findFirst();
            if (value.isEmpty()) throw new Exception("");
            name = Text.literal(value.get().icon.name);
        } catch (Exception e) {
            name = Text.literal(sound).setStyle(Style.EMPTY);
        }
        Utility.addLore(dfItem.getItemStack(),
                name,
                Text.empty(),
                Text.empty().append(Text.literal("Pitch: ").formatted(Formatting.GRAY)).append("%.2f".formatted(pitch)),
                Text.empty().append(Text.literal("Volume: ").formatted(Formatting.GRAY)).append("%.2f".formatted(volume)));
        return dfItem.getItemStack();
    }
}
