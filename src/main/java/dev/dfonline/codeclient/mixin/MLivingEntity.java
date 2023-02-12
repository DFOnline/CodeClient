package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MLivingEntity {
    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    private void jumpVelocity(CallbackInfoReturnable<Float> cir) {
        if(NoClip.ignoresWalls() && CodeClient.MC.player.getXRot() < -40) {
            cir.setReturnValue(NoClip.getJumpHeight());
        }
    }
}
