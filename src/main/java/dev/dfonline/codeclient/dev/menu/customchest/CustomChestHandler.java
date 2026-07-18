package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomChestHandler extends AbstractContainerMenu {
    private static final int Size = 9 * 3;
    public final CustomChestNumbers numbers = CustomChestNumbers.getSize();
    public final SimpleContainer inventory;
    public @NotNull Callback callback = (int slot, int revision, ItemStack stack) -> {

    };

    public CustomChestHandler(int syncId) {
        this(syncId, CodeClient.MC.player.getInventory(), new SimpleContainer(Size));
    }

    public CustomChestHandler(int syncId, Inventory playerInventory, SimpleContainer inventory) {
        super(new MenuType<>((syncId1, playerInventory1) -> new CustomChestHandler(syncId1), FeatureFlags.DEFAULT_FLAGS), syncId);
        this.inventory = inventory;
        checkContainerSize(inventory, Size);
        inventory.startOpen(playerInventory.player);

        for (int i = 0; i < Size; ++i) {
            Slot slot = new Slot(inventory, i, -10000, -10000);
            this.addSlot(slot);
        }

        addPlayerInventory(playerInventory);
    }

    @Override
    public void setItem(int slot, int revision, ItemStack stack) {
        super.setItem(slot, revision, stack);
        this.callback.run(slot, revision, stack);
    }

    @Override
    public Slot getSlot(int index) {
        return super.getSlot(index);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 1 + numbers.INVENTORY_X + column * 18, 1 + numbers.INVENTORY_Y + row * 18));
            }
        }

        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, numbers.INVENTORY_X + 1 + slot * 18, numbers.INVENTORY_Y + 59));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (slotIndex < 3 * 9) {
                if (!this.moveItemStackTo(stack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    public interface Callback {
        void run(int slot, int revision, ItemStack stack);
    }
}
