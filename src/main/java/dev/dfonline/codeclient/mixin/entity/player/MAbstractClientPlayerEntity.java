package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.dev.BuildPhaser;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class MAbstractClientPlayerEntity {
    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void isSpectator(CallbackInfoReturnable<Boolean> cir) {
        if (BuildPhaser.isClipping()) cir.setReturnValue(true);
    }
}
