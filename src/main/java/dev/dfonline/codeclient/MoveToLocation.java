package dev.dfonline.codeclient;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class MoveToLocation {
    private final ClientPlayerEntity player;
    public MoveToLocation(ClientPlayerEntity player) {
        this.player = player;
    }

    public static Vec3d shiftTowards(Vec3d origin, Vec3d location) {
        Vec3d pos = origin.relativize(location);
        double maxLength = 9.9;
        if(pos.length() > maxLength) {
            return origin.add(pos.normalize().multiply(maxLength));
        }
        else {
            return location;
        }
    }

    public void setPos(Vec3d pos) {
        setPos(pos.x, pos.y, pos.z);
    }
    public void setPos(double x, double y, double z) {
        this.player.setPos(x, y, z);
        this.player.teleport(x, y, z);
        CodeClient.MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
    }

    public static void shove(ClientPlayerEntity player, Vec3d location) {
        new MoveToLocation(player).teleportTowards(player.getPos(), location);
    }

    public void teleportTowards(Vec3d from, Vec3d to) {
        setPos(shiftTowards(from,to));
    }
}
