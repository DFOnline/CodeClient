package dev.dfonline.codeclient.action;

import net.minecraft.network.protocol.Packet;

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

    public void onTick() {}

    protected void callback() {
        this.callback.run();
    }

    public interface Callback {
        void run();
    }
}
