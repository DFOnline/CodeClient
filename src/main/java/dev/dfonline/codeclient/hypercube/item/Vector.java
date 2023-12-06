package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.item.Item;

public class Vector extends VarItem {
    private Double x;
    private Double y;
    private Double z;

    public Vector(Item material, JsonObject var) {
        super(material, var);
        this.x = data.get("x").getAsDouble();
        this.y = data.get("y").getAsDouble();
        this.z = data.get("z").getAsDouble();
    }

    public double getX() {
        return this.x;
    }
    public void setX(Double x) {
        this.x = x;
        CodeClient.LOGGER.info(String.valueOf(x));
        this.data.addProperty("x",x);
    }

    public double getY() {
        return this.y;
    }
    public void setY(Double y) {
        this.y = y;
        this.data.addProperty("x",y);
    }

    public double getZ() {
        return this.z;
    }
    public void setZ(Double z) {
        this.z = z;
        this.data.addProperty("x",z);
    }

    public void setCoords(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data.addProperty("x",x);
        this.data.addProperty("y",y);
        this.data.addProperty("z",z);
    }
}
