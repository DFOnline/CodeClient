package dev.dfonline.codeclient.mixin.network;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MClientConnection {
    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, net.minecraft.network.PacketListener listener, CallbackInfo ci) {
        if (CodeClient.handlePacket(packet)) ci.cancel();
    }
}