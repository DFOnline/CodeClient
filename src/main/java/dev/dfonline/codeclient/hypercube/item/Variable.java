package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Variable extends NamedItem {
    private Scope scope;

    public Variable(JsonObject var) {
        super(var);
        this.scope = Scope.valueOf(data.get("scope").getAsString());
    }

    public Variable() {
        super();
        this.setScope(Scope.unsaved);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
        data.addProperty("scope", scope.name());
    }

    @Override
    public String getId() {
        return "var";
    }

    @Override
    protected Item getIconItem() {
        return Items.MAGMA_CREAM;
    }

    @Override
    public ItemStack toStack() {
        var stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Text.literal(getName()).setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)));
        Utility.addLore(dfItem.getItemStack(), Text.literal(scope.longName).setStyle(Style.EMPTY.withColor(scope.color)));
        return dfItem.getItemStack();
    }
}
