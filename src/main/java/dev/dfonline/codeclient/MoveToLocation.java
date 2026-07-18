package dev.dfonline.codeclient;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

/**
 * Utility for moving the player.
 */
public class MoveToLocation {
    private final LocalPlayer player;

    public MoveToLocation(LocalPlayer player) {
        this.player = player;
    }

    /**
     * Shifts a location, not a player.
     * Will not shift more than 9.9 blocks, since the player cannot teleport further than that.
     */
    public static Vec3 shiftTowards(Vec3 origin, Vec3 location) {
        Vec3 pos = origin.vectorTo(location);
        double maxLength = 9.9;
        if (pos.length() > maxLength) {
            return origin.add(pos.normalize().scale(maxLength));
        } else {
            return location;
        }
    }

    public static void shove(LocalPlayer player, Vec3 location) {
        new MoveToLocation(player).teleportTowards(player.position(), location);
    }

    public void setPos(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
    }

    public void setPos(double x, double y, double z) {
        if (CodeClient.MC.getConnection() == null) return;
        if (new Vec3(x, y, x).distanceTo(player.position()) > 10) {
            // I've always done it like this, problems?
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
            CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(false, true));
        }
        this.player.setPosRaw(x, y, z);
        this.player.randomTeleport(x, y, z, false);
        CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, true));
    }

    /**
     * Moves the player up to 10 blocks towards `to` from `from`
     */
    public void teleportTowards(Vec3 from, Vec3 to) {
        setPos(shiftTowards(from, to));
    }
}
