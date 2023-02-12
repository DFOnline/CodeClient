package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.Action;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ClearPlot extends Action {
    public Step currentStep = Step.WAIT_FOR_OPTIONS;

    public ClearPlot(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        currentStep = Step.WAIT_FOR_OPTIONS;
        CodeClient.MC.player.commandUnsigned("plot clear");
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof ClientboundContainerSetContentPacket containerSetContentPacket) {
            if(currentStep == Step.WAIT_FOR_OPTIONS) {
                for (int i : List.of(9,14,15,44)) {
                    ItemStack itemStack = containerSetContentPacket.getItems().get(i);
                    Int2ObjectMap<ItemStack> modified = Int2ObjectMaps.singleton(i, new ItemStack(Items.AIR));
                    CodeClient.MC.getConnection().send(new ServerboundContainerClickPacket(containerSetContentPacket.getContainerId(), containerSetContentPacket.getStateId(), i, 0, ClickType.PICKUP, itemStack, modified));
                }
                currentStep = Step.WAIT_FOR_CLEAR;
                return true;
            }
            if(currentStep == Step.WAIT_FOR_CLEAR) {
                int i = 11;
                ItemStack itemStack = containerSetContentPacket.getItems().get(i);
                if(itemStack.getItem().equals(Items.GREEN_CONCRETE)) {
                    Int2ObjectMap<ItemStack> modified = Int2ObjectMaps.singleton(i, new ItemStack(Items.AIR));
                    CodeClient.MC.getConnection().send(new ServerboundContainerClickPacket(containerSetContentPacket.getContainerId(), containerSetContentPacket.getStateId(), i, 0, ClickType.PICKUP, itemStack, modified));
                    currentStep = Step.DONE;
                    this.callback();
                    return true;
                }
            }
        }
        if(currentStep != Step.DONE) {
            if(packet instanceof ClientboundContainerSetContentPacket) return true;
            if(packet instanceof ClientboundSoundPacket) return true;
            if(packet instanceof ClientboundOpenScreenPacket) return true;
            if(packet instanceof ClientboundContainerSetSlotPacket) return true;
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
