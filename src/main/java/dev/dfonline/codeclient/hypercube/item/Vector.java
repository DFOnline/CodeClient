package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Vector extends VarItem {
    private Double x;
    private Double y;
    private Double z;

    @Override
    public String getId() {
        return "vec";
    }

    @Override
    protected Item getIconItem() {
        return Items.PRISMARINE_SHARD;
    }

    @Override
    public JsonObject getDefaultData() {
        JsonObject object = new JsonObject();
        object.addProperty("x",0);
        object.addProperty("y",0);
        object.addProperty("z",0);
        return object;
    }

    public Vector(JsonObject var) {
        super(var);
        this.x = data.get("x").getAsDouble();
        this.y = data.get("y").getAsDouble();
        this.z = data.get("z").getAsDouble();
    }

    public Vector() {
        super();
        x = 0d;
        y = 0d;
        z = 0d;
    }

    public double getX() {
        return this.x;
    }

    public void setX(Double x) {
        this.x = x;
        CodeClient.LOGGER.info(String.valueOf(x));
        this.data.addProperty("x", x);
    }

    public double getY() {
        return this.y;
    }

    public void setY(Double y) {
        this.y = y;
        this.data.addProperty("x", y);
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(Double z) {
        this.z = z;
        this.data.addProperty("x", z);
    }

    public void setCoords(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data.addProperty("x", x);
        this.data.addProperty("y", y);
        this.data.addProperty("z", z);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Text.literal("Vector").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)));
        Utility.addLore(
                dfItem.getItemStack(),
                Text.empty().append(Text.literal("X: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.x)),
                Text.empty().append(Text.literal("Y: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.y)),
                Text.empty().append(Text.literal("Z: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.z))
        );
        return dfItem.getItemStack();
    }
}
