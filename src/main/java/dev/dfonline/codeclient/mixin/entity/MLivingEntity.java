package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MLivingEntity {
    @Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
    private void jumpVelocity(CallbackInfoReturnable<Float> cir) {
        if (NoClip.isIgnoringWalls() && CodeClient.MC.player.getPitch() <= Config.getConfig().UpAngle - 90) {
            cir.setReturnValue(NoClip.getJumpHeight());
        }
    }
}
