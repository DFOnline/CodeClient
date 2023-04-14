package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow public abstract boolean isSneaking();

    @Shadow public abstract void readNbt(NbtCompound nbt);

    @Shadow public abstract void requestTeleport(double destX, double destY, double destZ);

    @Shadow @Final protected static TrackedData<EntityPose> POSE;

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    private void insideWall(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.ignoresWalls()) cir.setReturnValue(false);
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if(CodeClient.MC.player == null) return;
        if(this.getId() != CodeClient.MC.player.getId()) return;
        if(NoClip.ignoresWalls()) {
            Vec3d pos = NoClip.handleClientPosition(movement);
            if(pos != null) {
                this.setPosition(pos);
                ci.cancel();
            }
        }
    }

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void setPose(EntityPose pose, CallbackInfo ci) {
        if(this.getId() != CodeClient.MC.player.getId()) return;
        if(!NoClip.ignoresWalls()) return;
        if(pose == EntityPose.STANDING) return;
        if(pose != EntityPose.CROUCHING) ci.cancel();
        if(!this.isSneaking()) ci.cancel();
    }
}
