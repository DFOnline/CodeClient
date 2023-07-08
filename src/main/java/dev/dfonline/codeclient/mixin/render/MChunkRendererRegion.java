package dev.dfonline.codeclient.mixin.render;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRendererRegion.class)
public class MChunkRendererRegion {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void getAppearance(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if(CodeClient.location instanceof Plot plot) {
            Boolean inPlot = plot.isInPlot(pos);
            if(inPlot != null && !inPlot) {
                cir.setReturnValue(Blocks.VOID_AIR.getDefaultState());
            }
            BlockState state = NoClip.replaceBlockAt(pos);
            if(state != null) cir.setReturnValue(state);
        }
    }

    @Inject(method = "getBlockState", at = @At("RETURN"))
    private void getSize(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockState block = cir.getReturnValue();
        if(block == null) return;
        if(CodeClient.location instanceof Plot plot) {
            if(plot.getX() == null) return;
            if(plot.getSize() != null) return;

            if(pos.getY() != 49) return;
            if(block.getBlock() != Blocks.STONE) return;

            if(pos.getX() + 1 > plot.getX()) return;
            if(pos.getX() < plot.getX() - 19) return;

            if(pos.getZ() == plot.getZ() + 50) plot.setSize(Plot.Size.BASIC);
            if(pos.getZ() == plot.getZ() + 100) plot.setSize(Plot.Size.LARGE);
            if(pos.getZ() == plot.getZ() + 300) plot.setSize(Plot.Size.MASSIVE);
        }
    }
}
