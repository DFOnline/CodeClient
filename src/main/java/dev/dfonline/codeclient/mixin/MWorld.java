package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.PlotLocation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MWorld {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void isPlaceable(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if(PlotLocation.isInCodeSpace(pos) && pos.getY() % 5 == 4) cir.setReturnValue(Blocks.BARRIER.getDefaultState());
    }
}
