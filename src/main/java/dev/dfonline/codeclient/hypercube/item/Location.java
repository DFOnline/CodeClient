package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class Location extends VarItem {
    private final JsonObject loc;
    private double x;
    private double y;
    private double z;
    private double pitch;
    private double yaw;

    public Location(JsonObject data) {
        super(data);
        this.loc = this.data.getAsJsonObject("loc");
        this.x = loc.get("x").getAsDouble();
        this.y = loc.get("y").getAsDouble();
        this.z = loc.get("z").getAsDouble();
        this.pitch = loc.get("pitch").getAsDouble();
        this.yaw = loc.get("yaw").getAsDouble();
    }

    @Override
    public String getId() {
        return "loc";
    }

    @Override
    protected Item getIconItem() {
        return Items.PAPER;
    }

    @Override
    public JsonObject getDefaultData() {
        var object = new JsonObject();
        object.addProperty("isBlock", false);
        var loc = new JsonObject();
        loc.addProperty("x", 0);
        loc.addProperty("y", 0);
        loc.addProperty("z", 0);
        loc.addProperty("pitch", 0);
        loc.addProperty("yaw", 0);
        object.add("loc", loc);
        return object;
    }

    public Location() {
        super();
        this.loc = this.data.getAsJsonObject("loc");
        this.x = loc.get("x").getAsDouble();
        this.y = loc.get("y").getAsDouble();
        this.z = loc.get("z").getAsDouble();
        this.pitch = loc.get("pitch").getAsDouble();
        this.yaw = loc.get("yaw").getAsDouble();
    }

    public Location(double x, double y, double z, double pitch, double yaw) {
        this();
        setX(x);
        setY(y);
        setZ(z);
        setPitch(pitch);
        setYaw(yaw);
    }

    public Location(Vec3d pos) {
        this();
        setX(pos.x);
        setY(pos.y);
        setZ(pos.z);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        loc.addProperty("x", x);
        data.add("loc", loc);
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        loc.addProperty("y", y);
        data.add("loc", loc);
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        loc.addProperty("z", z);
        data.add("loc", loc);
        this.z = z;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        loc.addProperty("pitch", pitch);
        data.add("loc", loc);
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        loc.addProperty("yaw", yaw);
        data.add("loc", loc);
        this.yaw = yaw;
    }

    /**
     * Mutates the location.
     *
     * @return The mutated location with the rotation.
     */
    public Location setRotation(double pitch, double yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        return this;
    }

    public void setCoords(double x, double y, double z, double pitch, double yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.loc.addProperty("x", x);
        this.loc.addProperty("y", y);
        this.loc.addProperty("z", z);
        this.loc.addProperty("pitch", pitch);
        this.loc.addProperty("yaw", yaw);
        data.add("loc", loc);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        DFItem dfItem = DFItem.of(stack);
        dfItem.setName(Text.literal("Location").setStyle(Style.EMPTY.withItalic(false).withColor(Icon.Type.LOCATION.color)));
        Utility.addLore(
                dfItem.getItemStack(),
                Text.empty().append(Text.literal("X: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.x)),
                Text.empty().append(Text.literal("Y: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.y)),
                Text.empty().append(Text.literal("Z: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.z)),
                Text.empty().append(Text.literal("p: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.pitch)),
                Text.empty().append(Text.literal("y: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.yaw))
        );
        return dfItem.getItemStack();
    }
}
