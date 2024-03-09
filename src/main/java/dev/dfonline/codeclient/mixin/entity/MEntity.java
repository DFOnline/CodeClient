package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MEntity {
    @Shadow public abstract boolean equals(Object o);
    @Shadow public abstract int getId();
    @Shadow protected abstract Vec3d adjustMovementForCollisions(Vec3d movement);

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    private void insideWall(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d adjustForCollision(Entity instance, Vec3d movement) {
        if(NoClip.isIgnoringWalls() && CodeClient.location instanceof Dev plot) return NoClip.adjustMovementForCollisions(instance, adjustMovementForCollisions(movement), plot);
        else return adjustMovementForCollisions(movement);
    }

//    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
//    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
//        if(!NoClip.isIgnoringWalls()) return;
//        if(CodeClient.MC.player == null) return;
//        if(this.getId() != CodeClient.MC.player.getId()) return;
//        Vec3d pos = NoClip.handleClientPosition(adjustMovementForCollisions(movement));
//        if(pos != null) {
//            this.setPosition(pos);
//            this.tryCheckBlockCollision();
//            ci.cancel();
//        }
//    }

//    @Inject(method = "wouldPoseNotCollide", at = @At("HEAD"), cancellable = true)
//    private void wouldPoseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
//        if(NoClip.isIgnoringWalls() && this.getId() == CodeClient.MC.player.getId()) {
//            cir.setReturnValue(true);
//        }
//    }
}
