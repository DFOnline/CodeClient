package dev.dfonline.codeclient.location;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.hypercube.ReferenceBook;
import net.minecraft.util.math.Vec3d;

public class Dev extends Creator {
    public Dev(double x, double z) {
        this.hasDev = true;
        setDevSpawn(x, z);
    }

    public void setDevSpawn(double x, double z) {
        setOrigin((int) (x + 11.5), (int) (z - 10.5));
    }

    public ReferenceBook getReferenceBook() {
        var player = CodeClient.MC.player;
        if (player == null) return null;
        var inventory = player.getInventory();

        for (int index = 0; index < inventory.size(); index++) {
            var itemStack = inventory.getStack(index);
            try {
                return new ReferenceBook(itemStack);
            } catch (IllegalArgumentException ignored) {

            }
        }
        return null;
    }

    public boolean isInDevSpace() {
        return isInDevSpace(CodeClient.MC.player.getEntityPos());
    }

    public boolean isInDevSpace(Vec3d pos) {
            assert CodeClient.MC.player != null;
            var size = assumeSize();
            if (getX() == null) return false;
            return pos.getX() <= getX() &&
                    pos.getZ() >= getZ() - NoClip.FREEDOM && pos.getZ() <= getZ() + size.codeLength + 1 + NoClip.FREEDOM &&
                    pos.getY() >= getFloorY() && pos.getY() < 256;
    }

    @Override
    public String name() {
        return "code";
    }
}
