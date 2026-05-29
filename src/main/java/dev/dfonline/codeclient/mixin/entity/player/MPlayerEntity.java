package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.BuildPhaser;
import dev.dfonline.codeclient.dev.Navigation;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MPlayerEntity extends LivingEntity {

    protected MPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        if (!(CodeClient.MC.player != null && CodeClient.MC.player.getAbilities().flying)) {
            CodeClient.getFeature(Navigation.class).map(Navigation::getAirSpeed).ifPresent(cir::setReturnValue);
        }
    }

    @Inject(method = "canChangeIntoPose", at = @At("HEAD"), cancellable = true)
    private void canChangeIntoPose(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) cir.setReturnValue(true);
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> cir) {
        var feat = CodeClient.getFeature(BuildPhaser.class);
        if (CodeClient.MC.player != null && this.getUuid() == CodeClient.MC.player.getUuid() && feat.isPresent() && feat.get().isClipping())
            cir.setReturnValue(true);
    }
}
