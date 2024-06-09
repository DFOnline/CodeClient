package dev.dfonline.codeclient.location;

public class Dev extends Creator {
    public Dev(double x, double z) {
        this.hasDev = true;
        setDevSpawn(x, z);
    }

    public void setDevSpawn(double x, double z) {
        setOrigin((int) (x + 10.5), (int) (z - 10.5));
    }

    @Override
    public String name() {
        return "code";
    }
}
