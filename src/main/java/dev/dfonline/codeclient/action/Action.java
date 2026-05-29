package dev.dfonline.codeclient.action;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.Feature;

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
