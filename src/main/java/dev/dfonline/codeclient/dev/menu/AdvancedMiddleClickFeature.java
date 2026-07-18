package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AdvancedMiddleClickFeature extends Feature {

    @Override
    public boolean enabled() {
        return activated();
    }

    public static boolean activated() {
        return Config.getConfig().AdvancedMiddleClick;
    }

    public ChestFeature makeChestFeature(AbstractContainerScreen<?> screen) {
        return new AdvancedMiddleClick(screen);
    }

    public static ItemStack getCopy(Slot slot, ItemStack cursor, ClickType actionType) {
        int max = slot.getItem().getMaxStackSize();
        boolean shift = actionType == ClickType.QUICK_MOVE; // TODO: verify this isn't inverted
        if (cursor.isEmpty() && slot.hasItem()) {
            int count = 1;
            if (shift) count = max;
            return slot.getItem().copyWithCount(count);
        } else if (!cursor.isEmpty() && compare(slot.getItem(), cursor)) {
            int count = Math.min(cursor.getCount()+1, max);
            if (shift) count = max;
            return slot.getItem().copyWithCount(count);
        }
        return null;
    }

    private static boolean compare(ItemStack a, ItemStack b) {
        if (a.is(b.getItemHolder())) {
            if (a.getHoverName().equals(b.getHoverName())) {
                return a.getComponents().equals(b.getComponents());
            }
        }
        return false;
    }

    private static class AdvancedMiddleClick extends ChestFeature {
        public AdvancedMiddleClick(AbstractContainerScreen<?> screen) {
            super(screen);
        }

        @Override
        public boolean clickSlot(Slot slot, int button, ClickType actionType, int syncId, int revision) {
            LocalPlayer player = Minecraft.getInstance().player;
            var manager = Minecraft.getInstance().gameMode;
            var network = Minecraft.getInstance().getConnection();
            if (!(manager == null || network == null || player == null) && actionType == ClickType.CLONE) {
                AbstractContainerMenu handler = player.containerMenu;

                // find an empty slot to use
                var emptySlotId = player.getInventory().getFreeSlot();
                int external = -1; // if the code had to use an external slot
                if (emptySlotId == -1) {
                    // no empty slot, try to create an available slot temporarily.
                    Optional<Slot> candidate = handler.slots.stream().filter(s -> !s.hasItem()).findFirst();
                    if (candidate.isPresent()) {
                        Slot selected = candidate.get();
                        external = selected.index;
                        manager.handleInventoryMouseClick(syncId, external, 0, ClickType.SWAP, player);
                        emptySlotId = 0;
                    } else {
                        // not possible to run the following code, return to vanilla behaviour
                        return false;
                    }
                }

                int convertedSlotId = Utility.getRemoteSlot(emptySlotId)-9+(handler.slots.size()-36);
                Slot emptySlot = handler.getSlot(convertedSlotId);
                var stack = getCopy(slot, handler.getCarried(), actionType); // get the stack to set the slot to
                if (stack == null) return false;

                if (!handler.getCarried().isEmpty()) {
                    manager.handleInventoryMouseClick(syncId, convertedSlotId, 0, ClickType.PICKUP, player);
                    emptySlot.getItem().setCount(0); // hides the temporary item in the empty slot.
                }

                // create the item for the server
                network.send(new ServerboundSetCreativeModeSlotPacket(
                        Utility.getRemoteSlot(emptySlotId), stack)
                );

                manager.handleInventoryMouseClick(syncId, convertedSlotId, 0, ClickType.PICKUP, player);

                if (external != -1) {
                    manager.handleInventoryMouseClick(syncId, external, 0, ClickType.SWAP, player);
                } else {
                    emptySlot.getItem().setCount(0); // hides the temporary item in the empty slot.
                }

                handler.setCarried(stack); // shows the item in your cursor before the server agrees.


                return true;
            }

            return false;
        }


    }
}
