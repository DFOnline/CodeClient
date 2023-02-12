package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MClientPlayNetworkHandler {
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if(CodeClient.onSendPacket(packet)) ci.cancel();
        if(CodeClient.currentAction.onSendPacket(packet)) ci.cancel();
    }
}
