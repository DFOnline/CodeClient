package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.Feature;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AdvancedMiddleClickFeature extends Feature {

    @Override
    public boolean enabled() {
        return true;
    }

    public ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new AdvancedMiddleClick(screen);
    }

    private static class AdvancedMiddleClick extends ChestFeature {
        public AdvancedMiddleClick(HandledScreen<?> screen) {
            super(screen);
        }

        @Override
        public boolean clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {


            return false;
        }


    }
}
