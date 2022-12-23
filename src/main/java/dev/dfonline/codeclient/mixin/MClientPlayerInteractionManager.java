package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MClientPlayerInteractionManager {
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    public void onAttackBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlockInternal", at = @At("HEAD") , cancellable = true)
    private void onInternalBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(InteractionManager.onPlaceBlock(hitResult.getBlockPos())) cir.setReturnValue(ActionResult.FAIL);
    }
}
