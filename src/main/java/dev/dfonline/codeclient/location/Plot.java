package dev.dfonline.codeclient.location;

public abstract class Plot extends Location {
    protected int id;
    protected String name;
    protected String owner;

    protected int originX;
    protected int originZ;

    protected boolean hasBuild;
    protected boolean hasDev;

    public void setOrigin(int x, int z) {
        this.originX = x;
        this.originZ = z;
    }

    public int getX() {
        return originX;
    }

    public int getZ() {
        return originZ;
    }
}
