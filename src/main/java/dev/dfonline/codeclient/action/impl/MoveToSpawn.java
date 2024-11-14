package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.MoveToLocation;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class MoveToSpawn extends Action {
    private final ClientPlayerEntity player = CodeClient.MC.player;
    public Step currentStep = Step.WAIT_FOR_TELEPORT;

    public MoveToSpawn(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        CodeClient.MC.getNetworkHandler().sendCommand("plot spawn");
        currentStep = Step.WAIT_FOR_TELEPORT;
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof PlayerPositionLookS2CPacket positionLookS2CPacket)
            return onTeleport(positionLookS2CPacket);
        if (packet instanceof PlaySoundS2CPacket && currentStep != Step.DONE) return true;
        return super.onReceivePacket(packet);
    }

    public boolean onTeleport(PlayerPositionLookS2CPacket packet) {
        Dev plot = (Dev) CodeClient.location;
        if (currentStep == Step.WAIT_FOR_TELEPORT) {
            plot.setDevSpawn(packet.change().position().x, packet.change().position().z);
            currentStep = Step.MOVE_TO_CORNER;
        }
        return false;
    }

    public boolean moveModifier() {
        Dev plot = (Dev) CodeClient.location;
        Vec3d location = new Vec3d(plot.getX(), plot.getFloorY(), plot.getZ());
        if (currentStep == Step.MOVE_TO_CORNER) {
            if (player.getPos().distanceTo(location) == 0) {
                currentStep = Step.DONE;
                this.callback();
                return false;
            }
            MoveToLocation.shove(player, location);
            return true;
        }
        return false;
    }

    enum Step {
        WAIT_FOR_TELEPORT,
        MOVE_TO_CORNER,
        DONE
    }
}
