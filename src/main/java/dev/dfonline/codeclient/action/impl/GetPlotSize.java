package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class GetPlotSize extends Action {
    private Step step = Step.WAIT;
    public GetPlotSize(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        ItemStack item = Items.PAPER.getDefaultStack();
        Utility.makeHolding(item);
    }

    @Override
    public void tick() {
        if (step == Step.WAIT) {
            step = Step.TP;
            CodeClient.MC.getNetworkHandler().sendChatCommand("/ptp 0 256 1000");
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
