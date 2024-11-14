package dev.dfonline.codeclient;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Utility for moving the player.
 */
public class MoveToLocation {
    private final ClientPlayerEntity player;

    public MoveToLocation(ClientPlayerEntity player) {
        this.player = player;
    }

    /**
     * Shifts a location, not a player.
     * Will not shift more than 9.9 blocks, since the player cannot teleport further than that.
     */
    public static Vec3d shiftTowards(Vec3d origin, Vec3d location) {
        Vec3d pos = origin.relativize(location);
        double maxLength = 9.9;
        if (pos.length() > maxLength) {
            return origin.add(pos.normalize().multiply(maxLength));
        } else {
            return location;
        }
    }

    public static void shove(ClientPlayerEntity player, Vec3d location) {
        new MoveToLocation(player).teleportTowards(player.getPos(), location);
    }

    public void setPos(Vec3d pos) {
        setPos(pos.x, pos.y, pos.z);
    }

    public void setPos(double x, double y, double z) {
        if (CodeClient.MC.getNetworkHandler() == null) return;
        if (new Vec3d(x, y, x).distanceTo(player.getPos()) > 10) {
            // I've always done it like this, problems?
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
            CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, true));
        }
        this.player.setPos(x, y, z);
        this.player.teleport(x, y, z, false);
        CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false, true));
    }

    /**
     * Moves the player up to 10 blocks towards `to` from `from`
     */
    public void teleportTowards(Vec3d from, Vec3d to) {
        setPos(shiftTowards(from, to));
    }
}
