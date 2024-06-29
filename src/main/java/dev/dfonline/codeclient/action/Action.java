package dev.dfonline.codeclient.action;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.location.Location;
import net.minecraft.network.packet.Packet;

public abstract class Action extends Feature {
    private final Callback callback;

    public Action(Callback callback) {
        this.callback = callback;
    }

    public abstract void init();

    protected void callback() {
        this.callback.run();
    }
}
