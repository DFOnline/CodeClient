package dev.dfonline.codeclient.location;

import net.minecraft.util.math.BlockPos;

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

    public void setSize(Size size) {
        this.size = size;
    }

    public Integer getX() {
        return originX;
    }

    public Integer getZ() {
        return originZ;
    }

    public boolean isInPlot(BlockPos pos) {
        if(size == null) return true;
        int x = pos.getX();
        int z = pos.getZ();
        // Signs can be out of the plot so blocks above the floor one outside the plot are shown.
        boolean inX = ((x >= originX - 20) || ((pos.getY() >= 50) && (x >= originX - 21))) && (x < originZ + size.size);
        boolean inZ = (z >= originZ) && (z <= originZ + size.size);

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
