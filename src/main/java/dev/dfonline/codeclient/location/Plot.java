package dev.dfonline.codeclient.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class Plot extends Location {
    protected Integer id;
    protected String name;
    protected String owner;

    protected Integer originX;
    protected Integer originZ;

    protected Boolean hasBuild;
    protected Boolean hasDev;

    protected Size size;

    public void setOrigin(int x, int z) {
        this.originX = x;
        this.originZ = z;
    }

    public void copyValuesFrom(Plot plot) {
        if(plot.id != null) this.id = plot.id;
        if(plot.name != null) this.name = plot.name;
        if(plot.owner != null) this.owner = plot.owner;
        if(plot.originX != null) this.originX = plot.originX;
        if(plot.originZ != null) this.originZ = plot.originZ;
        if(plot.hasBuild != null) this.hasBuild = plot.hasBuild;
        if(plot.hasDev != null) this.hasDev = plot.hasDev;
        if(plot.size != null) this.size = plot.size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Size getSize() {
        return size;
    }

    public Integer getX() {
        return originX;
    }

    public Integer getZ() {
        return originZ;
    }

    public Boolean isInPlot(BlockPos pos) {
        if(size == null) return null;

        return isInArea(pos.toCenterPos()) || isInDev(pos.toCenterPos());
    }

    /**
     * The play or build area.
     */
    public Boolean isInArea(Vec3d pos) {
        if(size == null) return null;

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = (x >= originX) && (x <= originX + size.size + 1);
        boolean inZ = (z >= originZ) && (z <= originZ + size.size + 1);

        return inX && inZ;
    }

    public Boolean isInDev(BlockPos pos) {
        return isInDev(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }
    public Boolean isInDev(Vec3d pos) {
        if(originX == null || originZ == null) return null;

        int x = (int) pos.getX();
        int z = (int) pos.getZ();

        boolean inX = (x < originX) && ((x >= originX - 19) || (pos.getY() >= 50) && (x >= originX - 20));
        boolean inZ = (z >= originZ) && (z <= originZ + (size != null ? size.size : 300));

        return inX && inZ;
    }

    public enum Size {
        BASIC(50),
        LARGE(100),
        MASSIVE(300);

        public final int size;
        Size(int size) {
            this.size = size;
        }
    }
}
