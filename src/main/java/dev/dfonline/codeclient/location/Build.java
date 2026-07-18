package dev.dfonline.codeclient.location;

import net.minecraft.world.phys.Vec3;

public class Build extends Creator {
    public Build() {
        this.hasBuild = true;
    }
    public Build(Vec3 pos) {
        this.hasBuild = true;
        this.buildPos = pos;
    }

    @Override
    public String name() {
        return "build";
    }
}
