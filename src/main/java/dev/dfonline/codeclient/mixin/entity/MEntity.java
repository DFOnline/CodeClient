package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MEntity {
    @Shadow public abstract boolean equals(Object o);
    @Shadow public abstract int getId();

    @Shadow public abstract void setPosition(Vec3d pos);

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    private void insideWall(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }

    @Inject(method = "isSubmergedIn", at = @At("HEAD"), cancellable = true)
    private void swimInDevSubmerged(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isInDevSpace() && fluidTag == FluidTags.WATER) {
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    private void swimInDevTouch(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isInDevSpace()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void swimInDev(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isInDevSpace()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if(CodeClient.MC.player == null) return;
        if(this.getId() != CodeClient.MC.player.getId()) return;
        if(NoClip.isIgnoringWalls()) {
            Vec3d pos = NoClip.handleClientPosition(movement);
            if(pos != null) {
                this.setPosition(pos);
                ci.cancel();
            }
        }
    }

    @Inject(method = "wouldPoseNotCollide", at = @At("HEAD"), cancellable = true)
    private void wouldPoseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if(this.getId() == CodeClient.MC.player.getId() && NoClip.isIgnoringWalls()) {
            cir.setReturnValue(true);
        }
    }
}
