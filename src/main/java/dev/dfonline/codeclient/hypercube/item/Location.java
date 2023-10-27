package dev.dfonline.codeclient.hypercube.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.NotImplementedException;

public class Location {
    public double x = 0;
    public double y = 0;
    public double z = 0;
    public double pitch = 0;
    public double yaw = 0;

    public  Location(double x, double y, double z, double pitch, double yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
    public Location(Vec3d pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }
    public Location(ItemStack item) {
        throw new NotImplementedException("Unimplemented!");
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

    public ItemStack toItemStack() {
        ItemStack location = Items.PAPER.getDefaultStack();
        NbtCompound compound = new NbtCompound();
        NbtCompound publicBukkitValues = new NbtCompound();
        publicBukkitValues.put("hypercube:varitem", NbtString.of("{\"id\":\"loc\",\"data\":{\"isBlock\":false,\"loc\":{\"x\":%.1f,\"y\":%.1f,\"z\":%.1f,\"pitch\":%.1f,\"yaw\":%.1f}}}".formatted(x, y, z, pitch, yaw)));
        compound.put("PublicBukkitValues", publicBukkitValues);
        location.setNbt(compound);

        return location;
    }
}
