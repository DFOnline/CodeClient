package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Event;
import dev.dfonline.codeclient.dev.Debug.Debug;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
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
        if(Debug.handlePacket(packet)) ci.cancel();
        Event.handlePacket(packet);
//        if(ChestPeeker.onPacket(packet)) ci.cancel();
    }
}