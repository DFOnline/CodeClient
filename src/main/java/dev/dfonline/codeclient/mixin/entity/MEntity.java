package dev.dfonline.codeclient.mixin.entity;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
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

    @Shadow protected abstract Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type);

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d adjustForCollision(Entity instance, Vec3d movement) {
        if(NoClip.isIgnoringWalls() && CodeClient.location instanceof Dev plot) return NoClip.adjustMovementForCollisions(instance, adjustMovementForCollisions(movement), plot);
        else return adjustMovementForCollisions(movement);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForSneaking(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d adjustForSneaking(Entity instance, Vec3d movement, MovementType type) {
        return NoClip.isIgnoringWalls() && CodeClient.location instanceof Dev ? movement : adjustMovementForSneaking(movement, type);
    }
}
