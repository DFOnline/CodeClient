package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.hypercube.item.Location;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Uses handlePacket, sendPacket and tick.
 */
public class GoTo extends Action {
    public final Vec3 target;
    boolean doNotSuppress = false;
    boolean complete = false;
    /**
     * If not null, we are using a location item to teleport.
     */
    private ItemStack locationItem;
    private boolean active;
    private int buffer = 0;
    private int lastTickPackets = 1;
    private int thisTickPackets = 0;

    public GoTo(Vec3 target, Callback callback) {
        super(callback);
        this.target = target;
    }

    @Override
    public void init() {
        active = true;
        if (CodeClient.MC.player == null) return;
        if (CodeClient.location instanceof Dev dev && dev.isInArea(target)) {
            locationItem = new Location(dev.getPos().vectorTo(target)).setRotation(CodeClient.MC.player.getXRot(), CodeClient.MC.player.getYRot()).toStack();
            locationItemTeleport();
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (locationItem == null) return super.onReceivePacket(packet);
        if (packet instanceof ClientboundPlayerPositionPacket tp) {
            if (CodeClient.MC.getConnection() == null || CodeClient.MC.player == null) return false;
            CodeClient.MC.getConnection().send(new ServerboundAcceptTeleportationPacket(tp.id()));
            CodeClient.MC.player.setPos(target);
            active = false;
            callback();
            return true;
        }
        return false;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerPositionPacket pos) {
            if (doNotSuppress) doNotSuppress = false;
            else return true;
        }
        return super.onSendPacket(packet);
    }

    @Override
    public void tick() {
        thisTickPackets = 0;
        if (CodeClient.MC.player == null || CodeClient.MC.getConnection() == null) {
            callback();
            return;
        }

        CodeClient.MC.player.setDeltaMovement(0, 0, 0);
        if (CodeClient.MC.player.position().equals(target)) {
            active = false;
            complete = true;
            callback();
            return;
        }
        if (locationItem != null) return;
        hackTowards();
        lastTickPackets = thisTickPackets;
    }

    private void locationItemTeleport() {
        LocalPlayer player = CodeClient.MC.player;
        ClientPacketListener net = CodeClient.MC.getConnection();
        if (player == null || net == null) return;
        ItemStack handItem = player.getMainHandItem();
        Utility.sendHandItem(locationItem);
        boolean sneaky = !player.isShiftKeyDown();
        if (sneaky) net.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
        net.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, player.blockPosition(), Direction.UP));
        net.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, player.blockPosition(), Direction.UP));
        if (sneaky) net.send(new ServerboundPlayerInputPacket(CodeClient.MC.player.getLastSentInput()));
        Utility.sendHandItem(handItem);
    }

    /**
     * Use hacky movement packets.
     */
    private void hackTowards() {
        if (CodeClient.MC.player == null || CodeClient.MC.getConnection() == null) return;

        int bufferStop = 1;
        int bufferReset = 0;

        buffer++;
        if (buffer > bufferStop) {
            if (buffer >= bufferReset) {
                buffer = 0;
            }
            return;
        }
        if (!active) return;
        if (CodeClient.MC.player == null) return;
        Vec3 pos = CodeClient.MC.player.position();

        Vec3 offset = pos.vectorTo(target);
        double maxLength = 50;
        double distance = offset.length();
        Vec3 jump = distance > maxLength ? pos.add(offset.normalize().scale(maxLength)) : target;
        if (distance > 10) {
            for (int i = 0; i < Math.min(lastTickPackets + 5, 50); i++) {
                thisTickPackets++;
                this.doNotSuppress = true;
                CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            }
        }
        thisTickPackets++;
        this.doNotSuppress = true;
        CodeClient.MC.player.setPosRaw(jump.x, jump.y, jump.z);
    }
}
