package dev.dfonline.codeclient.location;

public class Dev extends Creator {
    public Dev(double x, double z) {
        this.hasDev = true;
        setDevSpawn(x,z);
    }

    public void setDevSpawn(double x, double z) {
        setOrigin((int) (x + 9.5), (int) (z - 10.5));
    }

    @Deprecated
    public boolean isInCodeSpace(double x, double z) {
        return
                (x < originX) && (x >= originX - 20)
                        &&
                        (z >= originZ) && (z <= originZ + 301)
                ;
    }

    @Override
    public String name() {
        return "code";
    }
}
