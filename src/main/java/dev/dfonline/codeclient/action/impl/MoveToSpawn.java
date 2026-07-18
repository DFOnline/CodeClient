package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.MoveToLocation;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.phys.Vec3;

public class MoveToSpawn extends Action {
    private final LocalPlayer player = CodeClient.MC.player;
    public Step currentStep = Step.WAIT_FOR_TELEPORT;

    public MoveToSpawn(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        CodeClient.MC.getConnection().sendCommand("plot spawn");
        currentStep = Step.WAIT_FOR_TELEPORT;
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerPositionPacket positionLookS2CPacket)
            return onTeleport(positionLookS2CPacket);
        if (packet instanceof ClientboundSoundPacket && currentStep != Step.DONE) return true;
        return super.onReceivePacket(packet);
    }

    public boolean onTeleport(ClientboundPlayerPositionPacket packet) {
        Dev plot = (Dev) CodeClient.location;
        if (currentStep == Step.WAIT_FOR_TELEPORT) {
            plot.setDevSpawn(packet.change().position().x, packet.change().position().z);
            currentStep = Step.MOVE_TO_CORNER;
        }
        return false;
    }

    public boolean moveModifier() {
        Dev plot = (Dev) CodeClient.location;
        Vec3 location = new Vec3(plot.getX(), plot.getFloorY(), plot.getZ());
        if (currentStep == Step.MOVE_TO_CORNER) {
            if (player.position().distanceTo(location) == 0) {
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
