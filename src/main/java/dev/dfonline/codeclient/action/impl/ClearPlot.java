package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.Action;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class ClearPlot extends Action {
    public Step currentStep = Step.WAIT_FOR_OPTIONS;

    public ClearPlot(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        currentStep = Step.WAIT_FOR_OPTIONS;
        CodeClient.MC.getNetworkHandler().sendCommand("plot clear");
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof InventoryS2CPacket inventoryS2CPacket) {
            if(currentStep == Step.WAIT_FOR_OPTIONS) {
                for (int i : List.of(9,14,15,44)) {
                    ItemStack itemStack = inventoryS2CPacket.getContents().get(i);
                    Int2ObjectMap<ItemStack> modified = Int2ObjectMaps.singleton(i, new ItemStack(Items.AIR));
                    CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(inventoryS2CPacket.getSyncId(), inventoryS2CPacket.getRevision(), i, 0, SlotActionType.PICKUP, itemStack, modified));
                }
                currentStep = Step.WAIT_FOR_CLEAR;
                return true;
            }
            if(currentStep == Step.WAIT_FOR_CLEAR) {
                int i = 11;
                ItemStack itemStack = inventoryS2CPacket.getContents().get(i);
                if(itemStack.getItem().equals(Items.GREEN_CONCRETE)) {
                    Int2ObjectMap<ItemStack> modified = Int2ObjectMaps.singleton(i, new ItemStack(Items.AIR));
                    CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(inventoryS2CPacket.getSyncId(), inventoryS2CPacket.getRevision(), i, 0, SlotActionType.PICKUP, itemStack, modified));
                    currentStep = Step.DONE;
                    this.callback();
                    return true;
                }
            }
        }
        if(currentStep != Step.DONE) {
            if(packet instanceof InventoryS2CPacket) return true;
            if(packet instanceof PlaySoundS2CPacket) return true;
            if(packet instanceof OpenScreenS2CPacket) return true;
            if(packet instanceof ScreenHandlerSlotUpdateS2CPacket) return true;
        }
        return super.onReceivePacket(packet);
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        return super.onSendPacket(packet);
    }

    enum Step {
        WAIT_FOR_OPTIONS,
        WAIT_FOR_CLEAR,
        DONE
    }
}
