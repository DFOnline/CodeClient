package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Location;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public class DevForBuild extends Action {
    private GoTo move = null;
    private int timeout = 0;

    public DevForBuild(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        if(CodeClient.location instanceof Plot) {
            timeout = 0;
            CodeClient.MC.getNetworkHandler().sendCommand("dev");
        }
    }

    @Override
    public void onTick() {
        if(move != null) move.onTick();
        timeout += 1;
        if(timeout >= 30) {
            callback();
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof ScreenHandlerSlotUpdateS2CPacket) return true;
        if(move != null) return move.onReceivePacket(packet);
        return super.onReceivePacket(packet);
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if(move != null) return move.onSendPacket(packet);
        return super.onSendPacket(packet);
    }

    @Override
    public void onModeChange(Location location) {
        if(CodeClient.location instanceof Dev dev) {
            if(dev.getBuildPos() == null) {
                callback();
                return;
            }
            var player = CodeClient.MC.player;
            var flying = player.getAbilities().flying;
            player.setVelocity(0,0,0);
            move = new GoTo(dev.getBuildPos(), () -> {
                player.getInventory().clear();
                player.setYaw(0);
                player.setPitch(0);
                player.getAbilities().flying = flying;
                player.setVelocity(0,0,0);
                callback();
            });
            move.init();
        }
    }
}
