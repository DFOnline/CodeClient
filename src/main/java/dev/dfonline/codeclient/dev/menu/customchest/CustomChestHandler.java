package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class CustomChestHandler extends ScreenHandler {
    private final Inventory inventory;
    private static final int Size = 9*3;

    public CustomChestHandler(int syncId) {
        this(syncId, CodeClient.MC.player.getInventory(), new SimpleInventory(Size));
    }

    public CustomChestHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.HOPPER, syncId);
        this.inventory = inventory;
        checkSize(inventory, Size);
        inventory.onOpen(playerInventory.player);


        int row;
        int column;
        for(row = 0; row < 3; ++row) {
            for(column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }

//        for(row = 0; row < 3; ++row) {
//            for(column = 0; column < 9; ++column) {
//                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + i));
//            }
//        }
//
//        for(row = 0; row < 9; ++row) {
//            this.addSlot(new Slot(playerInventory, row, 8 + row * 18, 161 + i));
//        }

    }

//    private void addPlayerInventory(PlayerInventory playerInventory) {
//        int i = (3 - 4) * 18;
//        int row;
//        int column;
//
//        for(row = 0; row < 3; ++row) {
//            for(column = 0; column < 9; ++column) {
//                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + i));
//            }
//        }
//
//        for(row = 0; row < 9; ++row) {
//            this.addSlot(new Slot(playerInventory, row, 8 + row * 18, 161 + i));
//        }
//    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
