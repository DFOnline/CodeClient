package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.Navigation;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MPlayerEntity {
    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        if (!CodeClient.MC.player.getAbilities().flying) {
            CodeClient.getFeature(Navigation.class).map(Navigation::getAirSpeed).ifPresent(cir::setReturnValue);
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void jump(CallbackInfo ci) {
        if(CodeClient.getFeature(Navigation.class).map(Navigation::onJump).orElse(false)) {
            ci.cancel();
        }
    }

    @Inject(method = "canChangeIntoPose", at = @At("HEAD"), cancellable = true)
    private void canChangeIntoPose(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) cir.setReturnValue(true);
    }
}
