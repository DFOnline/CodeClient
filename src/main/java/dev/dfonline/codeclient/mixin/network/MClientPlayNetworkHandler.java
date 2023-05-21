package dev.dfonline.codeclient.mixin.network;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MClientPlayNetworkHandler {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if(CodeClient.onSendPacket(packet)) ci.cancel();
        if(CodeClient.currentAction.onSendPacket(packet)) ci.cancel();
    }
}
