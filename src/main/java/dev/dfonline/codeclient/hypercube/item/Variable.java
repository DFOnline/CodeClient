package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Variable extends NamedItem {
    private Scope scope;

    public Variable(Item material, JsonObject var) {
        super(material, var);
        this.scope = Scope.valueOf(data.get("scope").getAsString());
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        data.addProperty("scope",scope.name());
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public ItemStack toStack() {
        var stack = super.toStack();
        stack.setCustomName(Text.literal(getName()).setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)));
        Utility.addLore(stack, Text.literal(scope.longName).setStyle(Style.EMPTY.withColor(scope.color)));
        return stack;
    }
}
