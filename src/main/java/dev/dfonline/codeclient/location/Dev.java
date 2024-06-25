package dev.dfonline.codeclient.location;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.ReferenceBook;

public class Dev extends Creator {
    public Dev(double x, double z) {
        this.hasDev = true;
        setDevSpawn(x, z);
    }

    public void setDevSpawn(double x, double z) {
        setOrigin((int) (x + 10.5), (int) (z - 10.5));
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

    @Override
    public String name() {
        return "code";
    }
}
