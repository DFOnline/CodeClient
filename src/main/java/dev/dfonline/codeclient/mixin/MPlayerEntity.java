package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MPlayerEntity {

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        if(NoClip.ignoresWalls() && !CodeClient.MC.player.getAbilities().flying) {
            cir.setReturnValue(0.026F * (CodeClient.MC.player.getMovementSpeed() * Config.getConfig().AirSpeed));
        }
    }
}
