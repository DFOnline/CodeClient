package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.WorldPlot;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class GetPlotSize extends Action {
    private enum Step {
        WAIT,
        TP,
        DONE,
    }

    private Step step = Step.WAIT;
    private ItemStack recoverMainHand;

    public GetPlotSize(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        recoverMainHand = CodeClient.MC.player.getInventory().getStack(0);
        ItemStack item = Items.PAPER.getDefaultStack();
        NbtCompound compound = new NbtCompound();
        NbtCompound publicBukkitValues = new NbtCompound();
        publicBukkitValues.put("hypercube:varitem",NbtString.of("{\"id\":\"loc\",\"data\":{\"isBlock\":false,\"loc\":{\"x\":0.0,\"y\":256.0,\"z\":300.0,\"pitch\":0.0,\"yaw\":0.0}}}"));
        compound.put("PublicBukkitValues",publicBukkitValues);
        item.setNbt(compound);
        Utility.makeHolding(item);
    }

    @Override
    public void onTick() {
        if(step == Step.WAIT) {
            step = Step.TP;
            CodeClient.MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(CodeClient.MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            CodeClient.MC.interactionManager.attackBlock(CodeClient.MC.player.getBlockPos(), Direction.UP);
            CodeClient.MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            CodeClient.MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(CodeClient.MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            Utility.makeHolding(recoverMainHand);
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(CodeClient.location instanceof Dev plot) {
            if(step == Step.TP && packet instanceof PlayerPositionLookS2CPacket position) {
                step = Step.DONE;
                double size = position.getZ() - plot.getZ();
                if(size > 49) {
                    CodeClient.worldPlot = WorldPlot.Size.BASIC;
                }
                if(size > 99) {
                    CodeClient.worldPlot = WorldPlot.Size.LARGE;
                }
                if(size > 299) {
                    CodeClient.worldPlot = WorldPlot.Size.MASSIVE;
                }
                callback();
            }
        }
        return false;
    }
}
