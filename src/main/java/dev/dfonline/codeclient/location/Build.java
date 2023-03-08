package dev.dfonline.codeclient.location;

public class Build extends Plot {
    public Build() {
        this.hasBuild = true;
    }

    @Override
    public String name() {
        return "build";
    }
}
