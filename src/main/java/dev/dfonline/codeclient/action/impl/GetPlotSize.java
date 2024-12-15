package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class GetPlotSize extends Action {
    private Step step = Step.WAIT;
    private ItemStack recoverMainHand;
    public GetPlotSize(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        recoverMainHand = CodeClient.MC.player.getInventory().getStack(0);
        ItemStack item = Items.PAPER.getDefaultStack();
        DFItem dfItem = new DFItem(item);
        dfItem.getItemData().setHypercubeStringValue("varitem", "{\"id\":\"loc\",\"data\":{\"isBlock\":false,\"loc\":{\"x\":0.0,\"y\":256.0,\"z\":1000.0,\"pitch\":0.0,\"yaw\":0.0}}}");
        Utility.makeHolding(item);
    }

    @Override
    public void tick() {
        if (step == Step.WAIT) {
            step = Step.TP;
            CodeClient.MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(CodeClient.MC.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            CodeClient.MC.interactionManager.attackBlock(CodeClient.MC.player.getBlockPos(), Direction.UP);
            CodeClient.MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            CodeClient.MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(CodeClient.MC.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            if (recoverMainHand != null) Utility.makeHolding(recoverMainHand);
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (CodeClient.location instanceof Dev plot) {
            if (step == Step.TP && packet instanceof PlayerPositionLookS2CPacket position) {
                step = Step.DONE;
                double size = position.change().position().z - plot.getZ();
                if (size > 49) {
                    plot.setSize(Plot.Size.BASIC);
                }
                if (size > 99) {
                    plot.setSize(Plot.Size.LARGE);
                }
                if (size > 299) {
                    plot.setSize(Plot.Size.MASSIVE);
                }
                if(size > 999) {
                    plot.setSize(Plot.Size.MEGA);
                }
                callback();
            }
        }
        return false;
    }

    private enum Step {
        WAIT,
        TP,
        DONE,
    }
}
