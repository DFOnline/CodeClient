package dev.dfonline.codeclient;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlotLocation {
    private static int X;
    private static int Y;
    private static int Z;

    public static void set(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
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

    public static boolean isInCodeSpace(BlockPos pos) {
        return isInCodeSpace(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static boolean isInCodeSpace(Vec3d pos) {
        Vec3d plot = getAsVec3d();
        return
                (pos.x < plot.x) && (pos.x >= plot.x - 20)
                        &&
                (pos.z >= plot.z) && (pos.z <= plot.z + 301)
            ;
    }
}

