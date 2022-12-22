package dev.dfonline.codeclient;

import dev.dfonline.codeclient.mixin.MEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class DevClip {
    public static boolean ignoresWalls() {
        if(PlotLocation.getY() == 0) return false;
        if(CodeClient.MC.player.getY() < 50) return false;
        if(CodeClient.MC.player.getX() > PlotLocation.getX()) return false;
        return true;
    }

    public static Vec3d handleClientPosition(Vec3d movement) {
        ClientPlayerEntity player = CodeClient.MC.player;
        player.setPose(EntityPose.STANDING);
        return new Vec3d(Math.max(player.getX() + movement.x, PlotLocation.getX() - 22), Math.max(player.getY() + movement.y, 50), Math.max(player.getZ() + movement.z, PlotLocation.getZ() - 2));
    }

    public static Vec3d handleSeverPosition() {
        ClientPlayerEntity player = CodeClient.MC.player;
        Vec3d pos = new Vec3d(Math.max(player.getX(), PlotLocation.getX() - 20), (int) player.getY(), Math.max(player.getZ(), PlotLocation.getZ()));
        while (isInsideWall(pos)) {
            pos = pos.add(0,0.2,0);
        }
        return new Vec3d(pos.x, (int) pos.y, pos.z);
    }

    public static boolean isInsideWall(Vec3d playerPos) {
        Vec3d middlePos = playerPos.add(0,1.8 / 2,0);
        float f = 0.6F + 0.1F * 0.8F;
        Box box = Box.of(middlePos, f, (1.81 + 1.0E-6 * 0.8F), f);
        return BlockPos.stream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.world.getBlockState(pos);
            return !blockState.isAir() && VoxelShapes.matchesAnywhere(blockState.getCollisionShape(CodeClient.MC.world, pos).offset(pos.getX(), pos.getY(), pos.getZ()), VoxelShapes.cuboid(box), BooleanBiFunction.AND);
        });
    }
}
