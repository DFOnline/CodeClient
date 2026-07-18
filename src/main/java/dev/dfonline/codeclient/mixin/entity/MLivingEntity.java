package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.Navigation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MLivingEntity {
    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    private void jumpVelocity(CallbackInfoReturnable<Float> cir) {
        CodeClient.getFeature(Navigation.class).map(Navigation::jumpHeight).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if (CodeClient.getFeature(Navigation.class).map(Navigation::onJump).orElse(false)) {
            ci.cancel();
        }
    }
}
