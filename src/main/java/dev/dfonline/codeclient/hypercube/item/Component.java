package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.HypercubeMiniMessage;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Component extends NamedItem {
    public Component(JsonObject var) {
        super(var);
    }

    public Component() {
        super("Text");
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("name","Text");
        return super.getDefaultData();
    }

    @Override
    public String getId() {
        return "comp";
    }

    @Override
    protected Item getIconItem() {
        return Items.BOOK;
    }

    @Override
    public ItemStack toStack() {
        var display = HypercubeMiniMessage.MM.deserialize(getName())
                .applyFallbackStyle(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(false));
        var item = DFItem.of(super.toStack());
        item.setName(Utility.componentToText(display));
        return item.getItemStack();
    }
}
