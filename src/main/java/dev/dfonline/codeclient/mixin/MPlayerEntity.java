package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MPlayerEntity {
    @Shadow public abstract boolean isMainPlayer();

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void updatePose(CallbackInfo ci) {
        if(!this.isMainPlayer()) return;
        if(NoClip.ignoresWalls()) ci.cancel();
        CodeClient.MC.player.setPose(EntityPose.STANDING);
    }

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        if(NoClip.ignoresWalls()) {
            cir.setReturnValue(.07f * (CodeClient.MC.player.getMovementSpeed() * 10));
        }
    }
}
