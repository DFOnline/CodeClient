package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.Target;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

public class GameValue extends VarItem {
    private String type;
    private Target target;

    public GameValue(Item material, JsonObject var) {
        super(material, var);
        this.type = data.get("type").getAsString();
        this.target = Target.valueOf(data.get("target").getAsString());
    }

    public String getType() {
        return type;
    }

    // TODO: allow rejecting nonexistant values if actiondump is loaded
    public void setType(String type) {
        this.type = type;
        this.data.addProperty("type", type);
    }

//    public void setData(String type, Target target) {
//        this.type = type;
//        this.target = target;
//        this.data.add("type",type);
//        this.data.add("scope");
//    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
        this.data.addProperty("target", target.name());
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        try {
            ActionDump db = ActionDump.getActionDump();
            var value = Arrays.stream(db.gameValues).filter(gv -> gv.icon.getCleanName().equals(type)).findFirst();
            if (value.isEmpty()) throw new Exception("");
            stack.setCustomName(Text.literal(value.get().icon.name));
        } catch (Exception e) {
            stack.setCustomName(Text.literal(type).setStyle(Style.EMPTY));
        }
        Utility.addLore(stack, Text.literal("Target: ").formatted(Formatting.GRAY).append(Text.literal(target.name()).setStyle(Style.EMPTY.withColor(target.color))));
        return stack;
    }
}
