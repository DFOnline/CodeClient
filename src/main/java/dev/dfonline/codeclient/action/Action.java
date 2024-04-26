package dev.dfonline.codeclient.action;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.location.Location;
import net.minecraft.network.packet.Packet;

public abstract class Action {
    private final Callback callback;

    public Action(Callback callback) {
        this.callback = callback;
    }

    public abstract void init();

    public boolean onReceivePacket(Packet<?> packet) {
        return false;
    }

    public boolean onSendPacket(Packet<?> packet) {
        return false;
    }

    public void onTick() {
    }

    public void onModeChange(Location location) {

    }

    protected void callback() {
        this.callback.run();
    }
}
