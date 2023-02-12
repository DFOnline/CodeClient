package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.MoveToLocation;
import dev.dfonline.codeclient.PlotLocation;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public class MoveToSpawn extends Action {
    public Step currentStep = Step.WAIT_FOR_TELEPORT;

    private final LocalPlayer player = CodeClient.MC.player;

    public MoveToSpawn(Callback callback) {
        super(callback);
    }

    @Override
    public void init() {
        player.commandUnsigned("plot spawn");
        currentStep = Step.WAIT_FOR_TELEPORT;
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof ClientboundPlayerPositionPacket playerPositionPacket) return onTeleport(playerPositionPacket);
        if(packet instanceof ClientboundSoundPacket && currentStep != Step.DONE) return true;
        return super.onReceivePacket(packet);
    }

    public boolean onTeleport(ClientboundPlayerPositionPacket packet) {
        if(currentStep == Step.WAIT_FOR_TELEPORT) {
            PlotLocation.set(packet.getX() - -9.5, 50, packet.getZ() - 10.5);
            currentStep = Step.MOVE_TO_CORNER;
        }
        return false;
    }

    public boolean moveModifier() {
        if(currentStep == Step.MOVE_TO_CORNER) {
            if(player.position().distanceTo(PlotLocation.getAsVec3d()) == 0) {
                currentStep = Step.DONE;
                this.callback();
                return false;
            }
            MoveToLocation.shove(player, PlotLocation.getAsVec3d());
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
