package dev.dfonline.codeclient;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PlotLocation {
    private static int X = 0;
    private static int Y = 0;
    private static int Z = 0;

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

    public static Vec3 getAsVec3d() {
        return new Vec3(X,Y,Z);
    }

    public static boolean isInCodeSpace(BlockPos pos) {
        return isInCodeSpace(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static boolean isInCodeSpace(Vec3 pos) {
        Vec3 plot = getAsVec3d();
        return
                (pos.x < plot.x) && (pos.x >= plot.x - 20)
                        &&
                (pos.z >= plot.z) && (pos.z <= plot.z + 301)
            ;
    }
}

