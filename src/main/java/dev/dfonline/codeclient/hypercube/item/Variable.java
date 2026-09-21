package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
        dfItem.setName(Component.literal(getName()).setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE)));
        Utility.addLore(dfItem.getItemStack(), Component.literal(scope.longName).setStyle(Style.EMPTY.withColor(scope.color).withItalic(false)));
        return dfItem.getItemStack();
    }
}
