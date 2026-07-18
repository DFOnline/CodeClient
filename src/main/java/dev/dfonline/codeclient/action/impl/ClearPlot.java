package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.Action;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.List;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClearPlot extends Action {
    public Step currentStep = Step.WAIT_FOR_OPTIONS;

    public ClearPlot(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        currentStep = Step.WAIT_FOR_OPTIONS;
        CodeClient.MC.getConnection().sendCommand("plot clear");
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundContainerSetContentPacket inventoryS2CPacket) {

            if (CodeClient.MC.getConnection() == null) {
                currentStep = Step.DONE;
                this.callback();
                return true;
            }
            var hasher = CodeClient.MC.getConnection().decoratedHashOpsGenenerator();

            if (currentStep == Step.WAIT_FOR_OPTIONS) {
                for (int slot : List.of(9, 14, 15, 44)) {
                    HashedStack stack = HashedStack.create(inventoryS2CPacket.items().get(slot),hasher);
                    Int2ObjectMap<HashedStack> modified = Int2ObjectMaps.singleton(slot, HashedStack.create(Items.AIR.getDefaultInstance(), hasher));
                    CodeClient.MC.getConnection().send(new ServerboundContainerClickPacket(inventoryS2CPacket.containerId(),inventoryS2CPacket.stateId(),(short)slot,(byte)0,ClickType.PICKUP,modified,stack));
                }
                currentStep = Step.WAIT_FOR_CONFIRM;
                return true;
            }
            if (currentStep == Step.WAIT_FOR_CONFIRM) {
                short slot = 11;
                ItemStack itemStack = inventoryS2CPacket.items().get(slot);
                if (itemStack.getItem().equals(Items.GREEN_CONCRETE)) {
                    var air = HashedStack.create(Items.AIR.getDefaultInstance(), hasher);
                    Int2ObjectMap<HashedStack> modified = Int2ObjectMaps.singleton(slot, air);
                    CodeClient.MC.getConnection().send(new ServerboundContainerClickPacket(inventoryS2CPacket.containerId(),inventoryS2CPacket.stateId(),slot,(byte)0,ClickType.PICKUP,modified,air));
                    currentStep = Step.DONE;
                    this.callback();
                    return true;
                }
            }
        }
        if (currentStep != Step.DONE) {
            if (packet instanceof ClientboundContainerSetContentPacket) return true;
            if (packet instanceof ClientboundSoundPacket) return true;
            if (packet instanceof ClientboundOpenScreenPacket) return true;
            if (packet instanceof ClientboundContainerSetSlotPacket) return true;
        }
        return super.onReceivePacket(packet);
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        return super.onSendPacket(packet);
    }

    enum Step {
        WAIT_FOR_OPTIONS,
        WAIT_FOR_CONFIRM,
        DONE
    }
}
