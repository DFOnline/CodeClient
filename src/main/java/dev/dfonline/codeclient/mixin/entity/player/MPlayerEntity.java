package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.BuildPhaser;
import dev.dfonline.codeclient.dev.Navigation;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MPlayerEntity extends LivingEntity {

    protected MPlayerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        if (!(CodeClient.MC.player != null && CodeClient.MC.player.getAbilities().flying)) {
            CodeClient.getFeature(Navigation.class).map(Navigation::getAirSpeed).ifPresent(cir::setReturnValue);
        }
    }

    @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("HEAD"), cancellable = true)
    private void canChangeIntoPose(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) cir.setReturnValue(true);
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> cir) {
        var feat = CodeClient.getFeature(BuildPhaser.class);
        if (CodeClient.MC.player != null && this.getUUID() == CodeClient.MC.player.getUUID() && feat.isPresent() && feat.get().isClipping())
            cir.setReturnValue(true);
    }
}
