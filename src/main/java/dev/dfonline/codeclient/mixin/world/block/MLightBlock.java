package dev.dfonline.codeclient.mixin.world.block;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Creator;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightBlock.class)
public class MLightBlock {
    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void getRenderType(BlockState state, CallbackInfoReturnable<RenderShape> cir) {
        if (Config.getConfig().InvisibleBlocksInDev && CodeClient.location instanceof Creator)
            cir.setReturnValue(RenderShape.MODEL);
    }
}
