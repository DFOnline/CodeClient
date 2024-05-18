package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.dev.BuildPhaser;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MMouse {
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
    public boolean spectator(ClientPlayerEntity instance) {
        if(BuildPhaser.isClipping()) return false;
        else return instance.isSpectator();
    }
}
