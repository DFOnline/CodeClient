package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.Debug.Debug;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.dfonline.codeclient.CodeClient.MC;

@Mixin(ClientPlayerEntity.class)
public class MClientPlayerEntity {
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    private boolean lastSneaking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        CodeClient.onTick();
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        if(CodeClient.location instanceof Dev) {
            if (CodeClient.currentAction instanceof MoveToSpawn mts) if (mts.moveModifier()) ci.cancel();
            ClientPlayerEntity player = MC.player;
            if (NoClip.isIgnoringWalls()) {
                ci.cancel();
                Vec3d pos = NoClip.handleSeverPosition();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, player.getYaw(), player.getPitch(), false));
            }
            boolean sneaking = player.isSneaking();
            if (sneaking != this.lastSneaking) {
                ClientCommandC2SPacket.Mode mode = sneaking ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
                this.networkHandler.sendPacket(new ClientCommandC2SPacket(player, mode));
                this.lastSneaking = sneaking;
            }
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        if(NoClip.isIgnoringWalls()) ci.cancel();
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void slowDown(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }

    @Inject(method = "shouldAutoJump", at = @At("HEAD"), cancellable = true)
    private void autoJump(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }
}
