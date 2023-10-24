package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class GoTo extends Action {
    public final Vec3d target;
    private boolean active;
    private int buffer = 0;

    public GoTo(Vec3d target, Callback callback) {
        super(callback);
        this.target = target;
    }

    @Override
    public void init() {
        active = true;
    }

    @Override
    public void onTick() {
        if(CodeClient.MC.player == null || CodeClient.MC.getNetworkHandler() == null) {
            callback();
            return;
        }
        int bufferStop = 1;
        int bufferReset = 2;

        if(CodeClient.MC.player.getPos().equals(target)) {
            active = false;
            callback();
            return;
        }

        buffer++;
        if(buffer > bufferStop) {
            if(buffer >= bufferReset) {
                buffer = 0;
            }
            return;
        }
        if(!active) return;
        if(CodeClient.MC.player == null) return;
        Vec3d pos = CodeClient.MC.player.getPos();

        Vec3d offset = pos.relativize(target);
        double maxLength = 100;
        double distance = offset.length();
        Vec3d jump = distance > maxLength ? pos.add(offset.normalize().multiply(maxLength)) : target;
        if(distance > 10) {
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false));
        }
        CodeClient.MC.player.setPos(jump.x, jump.y, jump.z);
        CodeClient.MC.player.setVelocity(0,0,0);
    }
}
