package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.PlotLocation;
import dev.dfonline.codeclient.WorldPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class MWorld {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void isPlaceable(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if(WorldPlot.shouldNotRender(pos)) {
            cir.setReturnValue(Blocks.VOID_AIR.defaultBlockState());
        }
        if(PlotLocation.isInCodeSpace(pos) && pos.getY() % 5 == 4) cir.setReturnValue(Blocks.BARRIER.defaultBlockState());
    }
}
