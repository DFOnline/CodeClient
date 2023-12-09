package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class Location extends VarItem {
    private JsonObject loc;
    private double x;
    private double y;
    private double z;
    private double pitch;
    private double yaw;

    private static final JsonObject defaultObject;
    static {
        var obj = new JsonObject();
        obj.addProperty("id","loc");
        var data = new JsonObject();
        data.addProperty("isBlock",false);
        var loc = new JsonObject();
        loc.addProperty("x",0);
        loc.addProperty("y",0);
        loc.addProperty("z",0);
        loc.addProperty("pitch",0);
        loc.addProperty("yaw",0);
        data.add("loc",loc);
        obj.add("data",data);
        defaultObject = obj;
    }

    public Location(Item material, JsonObject data) {
        super(material, data);
        this.loc = this.data.getAsJsonObject("loc");
        this.x = loc.get("x").getAsDouble();
        this.y = loc.get("y").getAsDouble();
        this.z = loc.get("z").getAsDouble();
        this.pitch = loc.get("pitch").getAsDouble();
        this.yaw = loc.get("yaw").getAsDouble();
    }
    private Location() {
        this(Items.PAPER, defaultObject);
        // Make sure everything is init'd.
        setX(x);
        setY(y);
        setZ(z);
        setPitch(pitch);
        setYaw(yaw);
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

    public void setX(double x) {
        loc.addProperty("x",x);
        data.add("loc",loc);
        this.x = x;
    }
    public double getX() {
        return x;
    }
    public void setY(double y) {
        loc.addProperty("y",y);
        data.add("loc",loc);
        this.y = y;
    }
    public double getY() {
        return y;
    }
    public void setZ(double z) {
        loc.addProperty("z",z);
        data.add("loc",loc);
        this.z = z;
    }
    public double getZ() {
        return z;
    }
    public void setPitch(double pitch) {
        loc.addProperty("pitch",pitch);
        data.add("loc",loc);
        this.pitch = pitch;
    }
    public double getPitch() {
        return pitch;
    }
    public void setYaw(double yaw) {
        loc.addProperty("yaw",yaw);
        data.add("loc",loc);
        this.yaw = yaw;
    }
    public double getYaw() {
        return yaw;
    }

    /**
     * Mutates the location.
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
        this.loc.addProperty("x",x);
        this.loc.addProperty("y",y);
        this.loc.addProperty("z",z);
        this.loc.addProperty("pitch",pitch);
        this.loc.addProperty("yaw",yaw);
        data.add("loc",loc);
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        stack.setCustomName(Text.literal("Location").setStyle(Style.EMPTY.withItalic(false).withColor(Icon.Type.LOCATION.color)));
        var display = stack.getSubNbt("display");
        var lore = new NbtList();
        lore.add(Utility.nbtify(Text.empty().append(Text.literal("X: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.x))));
        lore.add(Utility.nbtify(Text.empty().append(Text.literal("Y: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.y))));
        lore.add(Utility.nbtify(Text.empty().append(Text.literal("Z: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.z))));
        lore.add(Utility.nbtify(Text.empty().append(Text.literal("p: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.z))));
        lore.add(Utility.nbtify(Text.empty().append(Text.literal("y: ").formatted(Formatting.GRAY)).append("%.2f".formatted(this.z))));
        display.put("Lore",lore);
        stack.setSubNbt("display",display);
        return stack;
    }
}
