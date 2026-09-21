package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Number extends NamedItem {
    @Override
    public String getId() {
        return "num";
    }

    @Override
    protected Item getIconItem() {
        return Items.SLIME_BALL;
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","0");
        return object;
    }

    public Number(JsonObject var) {
        super(var);
    }

    public Number() {
        super("0");
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Component.literal(this.getName()).setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)));
        return dfItem.getItemStack();
    }
}
