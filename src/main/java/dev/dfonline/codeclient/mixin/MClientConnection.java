package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MClientConnection {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, net.minecraft.network.listener.PacketListener listener, CallbackInfo ci) {
        if(CodeClient.handlePacket(packet)) ci.cancel();
        if(CodeClient.currentAction.onReceivePacket(packet)) ci.cancel();
    }
}