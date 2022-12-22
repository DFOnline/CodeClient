package dev.dfonline.codeclient;

import net.minecraft.util.math.Vec3d;

public class PlotLocation {
    private static int X;
    private static int Y;
    private static int Z;

    public static void set(int x, int y, int z) {
        X = x;
        Y = y;
        Z = Z;
    }
    public static void set(double x, double y, double z) {
        X = (int) x;
        Y = (int) y;
        Z = (int) z;
    }

    public static int getX() {
        return X;
    }
    public static int getY() {
        return Y;
    }
    public static int getZ() {
        return Z;
    }

    public static Vec3d getAsVec3d() {
        return new Vec3d(X,Y,Z);
    }
}

