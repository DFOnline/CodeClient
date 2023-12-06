package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
}
