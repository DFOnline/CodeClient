package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.Navigation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MLivingEntity {
    @Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
    private void jumpVelocity(CallbackInfoReturnable<Float> cir) {
        CodeClient.getFeature(Navigation.class).map(Navigation::jumpHeight).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if (CodeClient.getFeature(Navigation.class).map(Navigation::onJump).orElse(false)) {
            ci.cancel();
        }
    }
}
