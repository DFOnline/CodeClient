package dev.dfonline.codeclient;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class MoveToLocation {
    private final LocalPlayer player;
    public MoveToLocation(LocalPlayer player) {
        this.player = player;
    }

    public static Vec3 shiftTowards(Vec3 origin, Vec3 location) {
        Vec3 pos = origin.vectorTo(location);
        double maxLength = 9.9;
        if(pos.length() > maxLength) {
            return origin.add(pos.normalize().scale(maxLength));
        }
        else {
            return location;
        }
    }

    public void setPos(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
    }
    public void setPos(double x, double y, double z) {
        this.player.setPos(x, y, z);
        this.player.teleportToWithTicket(x, y, z);
        CodeClient.MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false));
    }

    public static void shove(LocalPlayer player, Vec3 location) {
        new MoveToLocation(player).teleportTowards(player.position(), location);
    }

    public void teleportTowards(Vec3 from, Vec3 to) {
        setPos(shiftTowards(from,to));
    }
}
