package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.Navigation;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MLivingEntity {
    @Inject(method = "getJumpVelocity", at = @At("RETURN"), cancellable = true)
    private void jumpVelocity(CallbackInfoReturnable<Float> cir) {
        CodeClient.getFeature(Navigation.class).map(Navigation::jumpHeight).ifPresent(cir::setReturnValue);
    }
}
