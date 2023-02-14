package dev.dfonline.codeclient.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MClientLevel {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "disconnect", at = @At("RETURN"))
    public void disconnect(CallbackInfo ci) {
        this.minecraft.stop();
    }
}
