package dev.dfonline.codeclient.mixin.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.Build;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrierBlock.class)
public class MBarrier {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void getRenderType(BlockState state, CallbackInfoReturnable<BlockRenderType> cir) {
        if (CodeClient.location instanceof Dev || CodeClient.location instanceof Build)
            cir.setReturnValue(BlockRenderType.MODEL);
    }
}
