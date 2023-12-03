package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;

public class Vector extends VarItem {
    private double x;
    private double y;
    private double z;

    public Vector(Item material, JsonObject var) {
        super(material, var);
        this.x = data.get("x").getAsDouble();
        this.y = data.get("y").getAsDouble();
        this.z = data.get("z").getAsDouble();
    }

    // This was written with three different cursors and xyz on the clipboard, it was actually loads of fun writing 3 functions at a time.
    public double getX() {
        return this.x;
    }
    public void setX(double x) {
        this.data.addProperty("x",x);
        this.x = x;
    }

    public double getY() {
        return this.y;
    }
    public void setY(double y) {
        this.data.addProperty("x",y);
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }
    public void setZ(double z) {
        this.data.addProperty("x",z);
        this.z = z;
    }
}
