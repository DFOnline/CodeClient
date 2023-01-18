package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.PlotLocation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public class NoClip {
    public static LineType display = null;

    public static boolean ignoresWalls() {
        if(PlotLocation.getY() == 0) return false;
        if(CodeClient.MC.player.getY() < 50) return false;
        if(CodeClient.MC.player.getX() > PlotLocation.getX()) return false;
        return true;
    }

    public static Vec3d handleClientPosition(Vec3d movement) {
        ClientPlayerEntity player = CodeClient.MC.player;
        player.setPose(EntityPose.STANDING);

        double nearestFloor = Math.floor(player.getY() / 5) * 5;
        Vec3d velocity = player.getVelocity();

        double x = Math.max(player.getX() + movement.x * 1.3, PlotLocation.getX() - 22);
        double y = Math.max(player.getY() + movement.y * 1, 50);
        double z = Math.max(player.getZ() + movement.z * 1.3, PlotLocation.getZ() - 2);

        player.setOnGround(false);
        boolean wantsToFall = player.isSneaking() && (player.getPitch() > 40);
        if((y < nearestFloor && !wantsToFall && !player.getAbilities().flying) || y == 50) {
            player.setVelocityClient(velocity.x, 0, velocity.z);
            y = nearestFloor;
            player.setOnGround(true);
        }

        if(x == PlotLocation.getX() - 22 && velocity.getX() < 0) player.setVelocityClient(0, velocity.y, velocity.z);
        if(z == PlotLocation.getZ() - 2  && velocity.getZ() < 0) player.setVelocityClient(velocity.x, velocity.y, 0);

        return new Vec3d(x, Math.min(y,256), z);
    }

    public static float getJumpHeight() {
        return 0.91f;
    }

    public static Vec3d handleSeverPosition() {
        ClientPlayerEntity player = CodeClient.MC.player;
        Vec3d pos = new Vec3d(Math.max(player.getX(), PlotLocation.getX() - 20), Math.ceil(player.getY()), Math.max(player.getZ(), PlotLocation.getZ()));
        if(isInsideWall(pos)) {
            double nearestFloor = Math.floor(player.getY() / 5) * 5;
            pos = new Vec3d(pos.x, nearestFloor + 2, pos.z);
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

    @Nullable
    public static BlockState replaceBlockAt(BlockPos pos) {
        if(!PlotLocation.isInCodeSpace(pos)) return null;
        if(display == null) return null;
        if(pos.getY() == 49) return null;
        if((pos.getY() + 1) % 5 != 0) return null;
        int offset = Math.abs((pos.getX() - 1) - PlotLocation.getX());
        if(offset >= 19) return null;
        if(display == LineType.NONE)                      return null;
        if(display == LineType.LINES  && offset % 3 >= 1) return Blocks.AIR.getDefaultState();
        if(display == LineType.DOUBLE && offset % 3 == 1) return Blocks.AIR.getDefaultState();
        return Blocks.RED_STAINED_GLASS.getDefaultState();
    }

    enum LineType {
        NONE,
        LINES,
        DOUBLE,
        SOLID,
    }
}
