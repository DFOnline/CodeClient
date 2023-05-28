package dev.dfonline.codeclient.location;

public class Build extends Creator {
    public Build() {
        this.hasBuild = true;
    }

    @Override
    public String name() {
        return "build";
    }
}
