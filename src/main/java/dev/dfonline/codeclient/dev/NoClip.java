package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.PlotLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class NoClip {
    public static LineType display = null;

    public static boolean ignoresWalls() {
        if(PlotLocation.getY() == 0) return false;
        if(CodeClient.MC.player.getY() < 50) return false;
        if(CodeClient.MC.player.getX() > PlotLocation.getX()) return false;
        return true;
    }

    public static Vec3 handleClientPosition(Vec3 movement) {
        LocalPlayer player = CodeClient.MC.player;
        player.setPose(Pose.STANDING);

        double nearestFloor = Math.floor(player.getY() / 5) * 5;
        Vec3 velocity = player.getDeltaMovement();

        double x = Math.max(player.getX() + movement.x * 1.3, PlotLocation.getX() - 22);
        double y = Math.max(player.getY() + movement.y * 1, 50);
        double z = Math.max(player.getZ() + movement.z * 1.3, PlotLocation.getZ() - 2);

        player.setOnGround(false);
        boolean wantsToFall = player.isCrouching() && (player.getXRot() > 40);
        if((y < nearestFloor && !wantsToFall && !player.getAbilities().flying) || y == 50) {
            player.setDeltaMovement(velocity.x, 0, velocity.z);
            y = nearestFloor;
            player.setOnGround(true);
        }

        if(x == PlotLocation.getX() - 22 && velocity.x() < 0) player.setDeltaMovement(0, velocity.y, velocity.z);
        if(z == PlotLocation.getZ() - 2  && velocity.z() < 0) player.setDeltaMovement(velocity.x, velocity.y, 0);

        return new Vec3(x, Math.min(y,256), z);
    }

    public static float getJumpHeight() {
        return 0.91f;
    }

    public static Vec3 handleSeverPosition() {
        LocalPlayer player = CodeClient.MC.player;
        Vec3 pos = new Vec3(Math.max(player.getX(), PlotLocation.getX() - 20), Math.ceil(player.getY()), Math.max(player.getZ(), PlotLocation.getZ()));
        if(isInsideWall(pos)) {
            double nearestFloor = Math.floor(player.getY() / 5) * 5;
            pos = new Vec3(pos.x, nearestFloor + 2, pos.z);
        }
        return new Vec3(pos.x, (int) pos.y, pos.z);
    }

    public static boolean isInsideWall(Vec3 playerPos) {
        Vec3 middlePos = playerPos.add(0,1.8 / 2,0);
        float f = 0.6F + 0.1F * 0.8F;
        AABB box = AABB.ofSize(middlePos, f, (1.81 + 1.0E-6 * 0.8F), f);
        return BlockPos.betweenClosedStream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.level.getBlockState(pos);
            return !blockState.isAir() && Shapes.joinIsNotEmpty(blockState.getCollisionShape(CodeClient.MC.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(box), BooleanOp.AND);
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
        if(display == LineType.LINES  && offset % 3 >= 1) return Blocks.AIR.defaultBlockState();
        if(display == LineType.DOUBLE && offset % 3 == 1) return Blocks.AIR.defaultBlockState();
        return Blocks.RED_STAINED_GLASS.defaultBlockState();
    }

    enum LineType {
        NONE,
        LINES,
        DOUBLE,
        SOLID,
    }
}
