package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Potion extends VarItem {
    private String potion;
    private int duration;
    private int amplifier;
    public static final int INFINITE = 1000000;

    @Override
    public String getId() {
        return "pot";
    }

    @Override
    protected Item getIconItem() {
        return Items.DRAGON_BREATH;
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("pot","Speed");
        object.addProperty("dur",1000000);
        object.addProperty("amp",0);
        return object;
    }

    public Potion(JsonObject var) {
        super(var);
        this.potion = data.get("pot").getAsString();
        this.duration = data.get("dur").getAsInt();
        this.amplifier = data.get("amp").getAsInt();
    }

    public Potion() {
        super();
        this.potion = "Speed";
        this.duration = 1000000;
        this.amplifier = 0;
    }

    public static String durationToString(int duration) {
        if (duration >= INFINITE) return "Infinite";
        if (duration % 20 != 0) return "%d ticks".formatted(duration);
        int seconds = duration / 20;
        return "%d:%02d".formatted(seconds / 60, seconds % 60);
    }

    public String getPotion() {
        return potion;
    }

    public void setPotion(String potion) {
        data.addProperty("pot", potion);
        this.potion = potion;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        data.addProperty("dur", duration);
        this.duration = duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        data.addProperty("amp", amplifier);
        this.amplifier = amplifier;
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Component.literal("Potion Effect").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE)));
        Component name;
        try {
            ActionDump db = ActionDump.getActionDump();
            var value = Arrays.stream(db.potions).filter(gv -> gv.icon.getCleanName().equals(potion)).findFirst();
            if (value.isEmpty()) throw new Exception("");
            name = Component.literal(value.get().icon.name);
        } catch (Exception e) {
            name = Component.literal(potion).setStyle(Style.EMPTY);
        }
        Utility.addLore(dfItem.getItemStack(),
                name,
                Component.empty(),
                Component.empty().append(Component.literal("Amplifier: ").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)).append(Component.literal(String.valueOf(amplifier + 1)).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))),
                Component.empty().append(Component.literal("Duration: ").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)).append(Component.literal(duration()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))))
        );
        return dfItem.getItemStack();
    }

    public String duration() {
        return durationToString(duration);
    }
}
