package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

public class AdvancedMiddleClickFeature extends Feature {

    @Override
    public boolean enabled() {
        return activated();
    }

    public static boolean activated() {
        return Config.getConfig().AdvancedMiddleClick;
    }

    public ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new AdvancedMiddleClick(screen);
    }

    public static ItemStack getCopy(Slot slot, ItemStack cursor) {
        int max = slot.getStack().getMaxCount();
        if (cursor.isEmpty() && slot.hasStack()) {
            int count = 1;
            if (Screen.hasShiftDown()) count = max;
            return slot.getStack().copyWithCount(count);
        } else if (!cursor.isEmpty() && compare(slot.getStack(), cursor)) {
            int count = Math.min(cursor.getCount()+1, max);
            if (Screen.hasShiftDown()) count = max;
            return slot.getStack().copyWithCount(count);
        }
        return null;
    }

    private static boolean compare(ItemStack a, ItemStack b) {
        if (a.itemMatches(b.getRegistryEntry())) {
            if (a.getName().equals(b.getName())) {
                return a.getComponents().equals(b.getComponents());
            }
        }
        return false;
    }

    private static class AdvancedMiddleClick extends ChestFeature {
        public AdvancedMiddleClick(HandledScreen<?> screen) {
            super(screen);
        }

        @Override
        public boolean clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            var manager = MinecraftClient.getInstance().interactionManager;
            var network = MinecraftClient.getInstance().getNetworkHandler();
            if (!(manager == null || network == null || player == null) && actionType == SlotActionType.CLONE) {
                ScreenHandler handler = player.currentScreenHandler;

                // find an empty slot to use
                var emptySlotId = player.getInventory().getEmptySlot();
                int external = -1; // if the code had to use an external slot
                if (emptySlotId == -1) {
                    // no empty slot, try to create an available slot temporarily.
                    Optional<Slot> candidate = handler.slots.stream().filter(s -> !s.hasStack()).findFirst();
                    if (candidate.isPresent()) {
                        Slot selected = candidate.get();
                        external = selected.id;
                        manager.clickSlot(syncId, external, 0, SlotActionType.SWAP, player);
                        emptySlotId = 0;
                    } else {
                        // not possible to run the following code, return to vanilla behaviour
                        return false;
                    }
                }

                int convertedSlotId = Utility.getRemoteSlot(emptySlotId)-9+(handler.slots.size()-36);
                Slot emptySlot = handler.getSlot(convertedSlotId);
                var stack = getCopy(slot, handler.getCursorStack()); // get the stack to set the slot to
                if (stack == null) return false;

                if (!handler.getCursorStack().isEmpty()) {
                    manager.clickSlot(syncId, convertedSlotId, 0, SlotActionType.PICKUP, player);
                    emptySlot.getStack().setCount(0); // hides the temporary item in the empty slot.
                }

                // create the item for the server
                network.sendPacket(new CreativeInventoryActionC2SPacket(
                        Utility.getRemoteSlot(emptySlotId), stack)
                );

                manager.clickSlot(syncId, convertedSlotId, 0, SlotActionType.PICKUP, player);

                if (external != -1) {
                    manager.clickSlot(syncId, external, 0, SlotActionType.SWAP, player);
//                    manager.clickSlot(syncId, 54, 0, SlotActionType.QUICK_CRAFT, player);
                } else {
                    emptySlot.getStack().setCount(0); // hides the temporary item in the empty slot.
                }

                handler.setCursorStack(stack); // shows the item in your cursor before the server agrees.


                return true;
            }

            return false;
        }


    }
}
