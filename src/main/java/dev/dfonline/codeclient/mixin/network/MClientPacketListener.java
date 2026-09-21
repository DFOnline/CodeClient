package dev.dfonline.codeclient.mixin.network;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MClientPacketListener {

    @Inject(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBundlePacket;subPackets()Ljava/lang/Iterable;"), cancellable = true)
    private void codeclient$handleBundlePacket(ClientboundBundlePacket packet, CallbackInfo ci) {
        ClientGamePacketListener listener = (ClientGamePacketListener) this;

        for (Packet<? super ClientGamePacketListener> subPacket : packet.subPackets()) {
            if (!CodeClient.handlePacket(subPacket)) {
                subPacket.handle(listener);
            }
        }

        ci.cancel();
    }

}
