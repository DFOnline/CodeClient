package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public class NoClip {
    public static final double PLAYER_FREEDOM = 0.621;
    /**
     * The extra blocks the player can fly outside in.
     */
    public static final double FREEDOM = 2;
    public static LineType display = null;
    public static Vec3d lastPos = null;
    public static float lastYaw = 0;
    public static float lastPitch = 0;
    public static int timesSinceMoved = 0;

    public static boolean isIgnoringWalls() {
        return CodeClient.noClipOn() && isInDevSpace();
    }

    public static boolean isInDevSpace() {
        if(CodeClient.location instanceof Dev plot) {
            assert CodeClient.MC.player != null;
            var size = plot.assumeSize();
            if (plot.getX() == null) return false;
            return CodeClient.MC.player.getX() <= plot.getX() &&
                    CodeClient.MC.player.getZ() >= plot.getZ() - FREEDOM && CodeClient.MC.player.getZ() <= plot.getZ() + size.codeLength + 1 + FREEDOM &&
                    CodeClient.MC.player.getY() >= plot.getFloorY() && CodeClient.MC.player.getY() < 256;
            }
        return false;
    }

    public static Vec3d handleClientPosition(Vec3d movement) {
        if (CodeClient.location instanceof Dev plot) {
            final int FREEDOM = 2;

            ClientPlayerEntity player = CodeClient.MC.player;

            double nearestFloor = Math.floor(player.getY() / 5) * 5;
            Vec3d velocity = player.getVelocity();

            var size = plot.assumeSize();
            double x = Math.max(player.getX() + movement.x * 1.3, plot.getX() - (size.codeWidth + FREEDOM));
            double y = Math.max(player.getY() + movement.y * 1, plot.getFloorY());
            double z = Math.max(player.getZ() + movement.z * 1.3, plot.getZ() - FREEDOM);
            z = Math.min(z, plot.getZ() + size.codeLength + 1 + FREEDOM);
            if (z == plot.getZ() + size.codeLength + 1 + FREEDOM && velocity.getZ() > 0)
                player.setVelocityClient(velocity.x, velocity.y, 0);
            if (z == plot.getZ() - 2 && velocity.getZ() < 0) player.setVelocityClient(velocity.x, velocity.y, 0);
            if (x == plot.getX() - (size.codeWidth + FREEDOM) && velocity.getX() < 0) player.setVelocityClient(0, velocity.y, velocity.z);

            player.setOnGround(false);
            boolean wantsToFall = !Config.getConfig().TeleportDown && player.isSneaking() && (player.getPitch() >= 90 - Config.getConfig().DownAngle);
            if ((y < nearestFloor && !wantsToFall && !player.getAbilities().flying) || y == plot.getFloorY()) {
                player.setVelocityClient(velocity.x, 0, velocity.z);
                y = nearestFloor;
                player.setOnGround(true);
            }

            return new Vec3d(x, Math.min(y, 256), z);
        }
        return null;
    }

    public static float getJumpHeight() {
        return 0.91f;
    }

    public static Vec3d handleSeverPosition() {
        if (CodeClient.location instanceof Dev plot) {
            ClientPlayerEntity player = CodeClient.MC.player;

            double z = Math.max(player.getZ(), plot.getZ() + PLAYER_FREEDOM);
            if (plot.getSize() != null) {
                z = Math.min(z, plot.getZ() + plot.getSize().codeLength + PLAYER_FREEDOM);
            }

            Vec3d pos = new Vec3d(Math.max(player.getX(), plot.getX() - (plot.assumeSize().codeWidth - PLAYER_FREEDOM)),
                    player.getY(),
                    z);

            if (isInsideWall(pos)) {
                double nearestFloor = Math.floor(player.getY() / 5) * 5;
                pos = new Vec3d(pos.x, nearestFloor + 2, pos.z);
            }
            return new Vec3d(pos.x, pos.y, pos.z);
        }
        return null;
    }

    public static boolean isInsideWall(Vec3d playerPos) {
        Vec3d middlePos = playerPos.add(0, 1.8 / 2, 0);
        float f = 0.6F + 0.1F * 0.8F;
        Box box = Box.of(middlePos, f, (1.799 + 1.0E-6 * 0.8F), f);
        return BlockPos.stream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.world.getBlockState(pos);
            return !blockState.isAir() && VoxelShapes.matchesAnywhere(blockState.getCollisionShape(CodeClient.MC.world, pos).offset(pos.getX(), pos.getY(), pos.getZ()), VoxelShapes.cuboid(box), BooleanBiFunction.AND);
        });
    }

    @Nullable
    public static BlockState replaceBlockAt(BlockPos pos) {
        if (CodeClient.location instanceof Dev plot) {
            Boolean isInDev = plot.isInDev(pos.toCenterPos());
            if (isInDev == null || !isInDev) return null;
            if (display == null) return null;
            if (pos.getY() == 49) return null;
            if ((pos.getY() + 1) % 5 != 0) return null;
            int offset = Math.abs((pos.getX() - 1) - plot.getX());
            if (offset >= 19) return null;
            if (display == LineType.NONE) return null;
            if (display == LineType.LINES && offset % 3 >= 1) return Blocks.AIR.getDefaultState();
            if (display == LineType.DOUBLE && offset % 3 == 1) return Blocks.AIR.getDefaultState();
            return Blocks.RED_STAINED_GLASS.getDefaultState();
        }
        return null;
    }

    enum LineType {
        NONE,
        LINES,
        DOUBLE,
        SOLID,
    }
}
