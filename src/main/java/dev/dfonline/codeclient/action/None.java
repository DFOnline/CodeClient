package dev.dfonline.codeclient.action;

import dev.dfonline.codeclient.OverlayManager;

public class None extends Action {
    public None() {
        super(() -> {
        });
        this.init();
    }

    @Override
    public void init() {
        OverlayManager.setOverlayText();
    }
}
