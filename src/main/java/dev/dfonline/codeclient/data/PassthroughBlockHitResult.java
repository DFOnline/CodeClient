package dev.dfonline.codeclient.data;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/* Used to allow interactBlock calls caused by middle click to pass through without being cancelled for the backwarp functionality */

public class PassthroughBlockHitResult extends BlockHitResult {
    public PassthroughBlockHitResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock) {
        super(pos, side, blockPos, insideBlock);
    }
}
