package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class MClientPlayerEntity {
    @Shadow @Final public ClientPacketListener connection;

    private boolean lastSneaking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        CodeClient.onTick();
        CodeClient.currentAction.onTick();
        if(NoClip.ignoresWalls()) CodeClient.MC.player.noPhysics = true;
//        ChestPeeker.tick();
    }

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        if(CodeClient.currentAction instanceof MoveToSpawn mts) if(mts.moveModifier()) ci.cancel();
        if(NoClip.ignoresWalls()) {
        ci.cancel();
            Vec3 pos = NoClip.handleSeverPosition();
            LocalPlayer player = CodeClient.MC.player;
            this.connection.send(new ServerboundMovePlayerPacket.PosRot(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot(), false));

            boolean sneaking = player.isCrouching();
            if (sneaking != this.lastSneaking) {
                ServerboundPlayerCommandPacket.Action mode = sneaking ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
                this.connection.send(new ServerboundPlayerCommandPacket(player, mode));
                this.lastSneaking = sneaking;
            }
        }
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        if(NoClip.ignoresWalls()) ci.cancel();
    }

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void slowDown(CallbackInfoReturnable<Boolean> cir) {
        if(NoClip.ignoresWalls()) cir.setReturnValue(false);
    }
}
