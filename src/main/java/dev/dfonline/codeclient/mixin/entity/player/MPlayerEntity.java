package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.NoClip;
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
        if (NoClip.isInDevSpace() && !CodeClient.MC.player.getAbilities().flying) {
            cir.setReturnValue(0.026F * (CodeClient.MC.player.getMovementSpeed() * Config.getConfig().AirSpeed));
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void jump(CallbackInfo ci) {
        if (InteractionManager.shouldTeleportUp()) {
            Vec3d move = CodeClient.MC.player.getPos().add(0, 5, 0);
            if ((!NoClip.isIgnoringWalls()) && NoClip.isInsideWall(move)) move = move.add(0, 2, 0);
            CodeClient.MC.player.setPosition(move);
            ci.cancel();
        }
    }

    @Inject(method = "canChangeIntoPose", at = @At("HEAD"), cancellable = true)
    private void canChangeIntoPose(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if (NoClip.isIgnoringWalls()) cir.setReturnValue(true);
    }
}
