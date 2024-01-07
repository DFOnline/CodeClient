package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class CustomChestHandler extends ScreenHandler {
    private final Inventory inventory;
    private static final int Size = 9*3;
    public final CustomChestNumbers numbers = CustomChestNumbers.getSize();
    public @NotNull Callback callback = (int slot, int revision, ItemStack stack) -> {

    };
    public interface Callback {
        void run(int slot, int revision, ItemStack stack);
    }

    public CustomChestHandler(int syncId) {
        this(syncId, CodeClient.MC.player.getInventory(), new SimpleInventory(Size));
    }

    public CustomChestHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(new ScreenHandlerType<>((syncId1, playerInventory1) -> new CustomChestHandler(syncId1), FeatureFlags.DEFAULT_ENABLED_FEATURES), syncId);
        this.inventory = inventory;
        checkSize(inventory, Size);
        inventory.onOpen(playerInventory.player);

        for(int i = 0; i < Size; ++i) {
            Slot slot = new Slot(inventory,i,-10000,-10000);
            this.addSlot(slot);
        }

        addPlayerInventory(playerInventory);
    }

    @Override
    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        super.setStackInSlot(slot, revision, stack);
        this.callback.run(slot,revision,stack);
    }

    @Override
    public Slot getSlot(int index) {
        return super.getSlot(index);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 1 + numbers.INVENTORY_X + column * 18, 1 + numbers.INVENTORY_Y + row * 18));
            }
        }

        for(int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, numbers.INVENTORY_X + 1 + slot * 18, numbers.INVENTORY_Y + 59));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            itemStack = stack.copy();
            if (slotIndex < 3 * 9) {
                if (!this.insertItem(stack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
