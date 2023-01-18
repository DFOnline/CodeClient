package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.PlotLocation;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.block.BlockState;
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
        BlockState state = NoClip.replaceBlockAt(pos);
        if(state != null) cir.setReturnValue(state);
    }
}
