package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.NotImplementedException;

public class Location extends VarItem {
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private double pitch = 0;
    private double yaw = 0;

    public Location(Item material, JsonObject data) {
        super(material, data);
    }
    private Location() {
        this(Items.PAPER, new JsonObject());
        // Make sure `data` is filled with values.
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
        data.addProperty("x",x);
        this.x = x;
    }
    public double getX() {
        return x;
    }
    public void setY(double y) {
        data.addProperty("y",y);
        this.y = y;
    }
    public double getY() {
        return y;
    }
    public void setZ(double z) {
        data.addProperty("z",z);
        this.z = z;
    }
    public double getZ() {
        return z;
    }
    public void setPitch(double pitch) {
        data.addProperty("pitch",pitch);
        this.pitch = pitch;
    }
    public double getPitch() {
        return pitch;
    }
    public void setYaw(double yaw) {
        data.addProperty("yaw",yaw);
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
