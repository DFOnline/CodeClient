package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.WorldPlot;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderChunkRegion.class)
public class MChunkRendererRegion {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void getAppearance(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if(WorldPlot.shouldNotRender(pos)) {
                cir.setReturnValue(Blocks.VOID_AIR.defaultBlockState());
        }
        BlockState state = NoClip.replaceBlockAt(pos);
        if(state != null) cir.setReturnValue(state);
    }
}
