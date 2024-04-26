package dev.dfonline.codeclient.location;

import net.minecraft.util.math.Vec3d;

public class Build extends Creator {
    public Build() {
        this.hasBuild = true;
    }
    public Build(Vec3d pos) {
        this.hasBuild = true;
        this.buildPos = pos;
    }

    @Override
    public String name() {
        return "build";
    }
}
