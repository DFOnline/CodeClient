package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class NoClip extends Feature {
    public static final double PLAYER_FREEDOM = 0.621;
    /**
     * The extra blocks the player can fly outside in.
     */
    public static final double FREEDOM = 2;
    public LineType display = null;
    public Vec3 lastPos = null;
    public float lastYaw = 0;
    public float lastPitch = 0;
    public int timesSinceMoved = 0;

    @Override
    public boolean enabled() {
        return Config.getConfig().NoClipEnabled;
    }

    public boolean isIgnoringWalls() {
        Optional<BuildPhaser> buildPhaser = CodeClient.getFeature(BuildPhaser.class);
        return CodeClient.location instanceof Dev dev && CodeClient.noClipOn() && dev.isInDevSpace() && !buildPhaser.get().isClipping();
    }

    public Vec3 handleClientPosition(Vec3 movement) {
        if (CodeClient.location instanceof Dev plot) {
            final int FREEDOM = 2;

            LocalPlayer player = CodeClient.MC.player;

            double nearestFloor = Math.floor(player.getY() / 5) * 5;
            Vec3 velocity = player.getDeltaMovement();

            var size = plot.assumeSize();
            double x = Math.max(player.getX() + movement.x * 1.3, plot.getX() - (size.codeWidth + FREEDOM));
            double y = Math.max(player.getY() + movement.y * 1, plot.getFloorY());
            double z = Math.max(player.getZ() + movement.z * 1.3, plot.getZ() - FREEDOM);
            z = Math.min(z, plot.getZ() + size.codeLength + 1 + FREEDOM);
            if (z == plot.getZ() + size.codeLength + 1 + FREEDOM && velocity.z() > 0)
                player.lerpMotion(new Vec3(velocity.x, velocity.y, 0));
            if (z == plot.getZ() - 2 && velocity.z() < 0) player.lerpMotion(new Vec3(velocity.x, velocity.y, 0));
            if (x == plot.getX() - (size.codeWidth + FREEDOM) && velocity.x() < 0) player.lerpMotion(new Vec3(0, velocity.y, velocity.z));

            player.setOnGround(false);
            boolean wantsToFall = !Config.getConfig().TeleportDown && player.isShiftKeyDown() && (player.getXRot() >= 90 - Config.getConfig().DownAngle);
            if ((y < nearestFloor && !wantsToFall && !player.getAbilities().flying) || y == plot.getFloorY()) {
                player.lerpMotion(new Vec3(velocity.x, 0, velocity.z));
                y = nearestFloor;
                player.setOnGround(true);
            }

            return new Vec3(x, Math.min(y, 256), z);
        }
        return null;
    }

    public Vec3 handleSeverPosition() {
        if (CodeClient.location instanceof Dev plot) {
            LocalPlayer player = CodeClient.MC.player;

            double z = Math.max(player.getZ(), plot.getZ() + PLAYER_FREEDOM);
            if (plot.getSize() != null) {
                z = Math.min(z, plot.getZ() + plot.getSize().codeLength + PLAYER_FREEDOM);
            }

            Vec3 pos = new Vec3(Math.max(player.getX(), plot.getX() - (plot.assumeSize().codeWidth - PLAYER_FREEDOM)),
                    player.getY(),
                    z);

            if (isInsideWall(pos)) {
                double nearestFloor = Math.floor(player.getY() / 5) * 5;
                pos = new Vec3(pos.x, nearestFloor + 2, pos.z);
            }
            return new Vec3(pos.x, pos.y, pos.z);
        }
        return null;
    }

    public boolean isInsideWall(Vec3 playerPos) {
        Vec3 middlePos = playerPos.add(0, 1.8 / 2, 0);
        float f = 0.6F + 0.1F * 0.8F;
        AABB box = AABB.ofSize(middlePos, f, (1.799 + 1.0E-6 * 0.8F), f);
        return BlockPos.betweenClosedStream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.level.getBlockState(pos);
            return !blockState.isAir() && Shapes.joinIsNotEmpty(blockState.getCollisionShape(CodeClient.MC.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(box), BooleanOp.AND);
        });
    }

    @Nullable
    public BlockState replaceBlockAt(BlockPos pos) {
        if (CodeClient.location instanceof Dev plot) {
            Boolean isInDev = plot.isInDev(Vec3.atCenterOf(pos));
            if (isInDev == null || !isInDev) return null;
            if (display == null) return null;
            if (pos.getY() == 49) return null;
            if ((pos.getY() + 1) % 5 != 0) return null;
            int offset = Math.abs((pos.getX() - 1) - plot.getX());
            if (offset >= 19) return null;
            if (display == LineType.NONE) return null;
            if (display == LineType.LINES && offset % 3 >= 1) return Blocks.AIR.defaultBlockState();
            if (display == LineType.DOUBLE && offset % 3 == 1) return Blocks.AIR.defaultBlockState();
            return Blocks.STAINED_GLASS.red().defaultBlockState();
        }
        return null;
    }

    @Override
    public void tick() {
        if(isIgnoringWalls()) CodeClient.MC.player.noPhysics = true;
    }

    enum LineType {
        NONE,
        LINES,
        DOUBLE,
        SOLID,
    }
}
