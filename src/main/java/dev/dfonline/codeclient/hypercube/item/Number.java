package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.getName()).setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)));
        return stack;
    }
}
