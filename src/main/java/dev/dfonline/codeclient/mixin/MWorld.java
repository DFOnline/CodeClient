package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.WorldPlot;
import dev.dfonline.codeclient.location.Dev;
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
        Dev plot = (Dev) CodeClient.location;
        if(WorldPlot.shouldNotRender(pos)) {
            cir.setReturnValue(Blocks.VOID_AIR.getDefaultState());
        }
        if(plot.isInCodeSpace(pos.getX(), plot.getZ()) && pos.getY() % 5 == 4) cir.setReturnValue(Blocks.BARRIER.getDefaultState());
    }
}
