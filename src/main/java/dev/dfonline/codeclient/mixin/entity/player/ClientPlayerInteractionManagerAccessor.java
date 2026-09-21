package dev.dfonline.codeclient.mixin.entity.player;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Invoker("startPrediction")
    void invokeSequencedPacket(ClientLevel world, PredictiveAction packetCreator);
}
