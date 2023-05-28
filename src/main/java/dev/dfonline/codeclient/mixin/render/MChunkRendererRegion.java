package dev.dfonline.codeclient.mixin.render;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
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
        if(CodeClient.location instanceof Dev dev) {
            Boolean inPlot = dev.isInPlot(pos);
            if(inPlot != null && !inPlot) {
                cir.setReturnValue(Blocks.VOID_AIR.getDefaultState());
            }
            BlockState state = NoClip.replaceBlockAt(pos);
            if(state != null) cir.setReturnValue(state);
        }
    }
}
