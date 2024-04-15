package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.dfonline.codeclient.CodeClient.MC;

@Mixin(ClientPlayerEntity.class)
public abstract class MClientPlayerEntity {
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    private boolean inSneakingPose;
    private boolean lastSneaking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        CodeClient.onTick();
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        if (CodeClient.location instanceof Dev plot) {
            if (CodeClient.currentAction instanceof MoveToSpawn mts) if (mts.moveModifier()) ci.cancel();
            ClientPlayerEntity player = MC.player;
            if (NoClip.isIgnoringWalls()) {
                if (Config.getConfig().TeleportDown && player.getY() % 5 == 0 && !lastSneaking && player.isSneaking() && (player.getPitch() >= 90 - Config.getConfig().DownAngle)) {
                    Vec3d move = CodeClient.MC.player.getPos().add(0, -5, 0);
                    if (move.y < plot.getFloorY()) move = new Vec3d(move.x, plot.getFloorY(), move.z);
                    if ((!NoClip.isIgnoringWalls()) && NoClip.isInsideWall(move)) move = move.add(0, 2, 0);
                    CodeClient.MC.player.setPosition(move);
                }
                ci.cancel();
                Vec3d pos = NoClip.handleSeverPosition();
                boolean idle = NoClip.timesSinceMoved++ > 40;
                if (idle) NoClip.timesSinceMoved = 0;
                float yaw = player.getYaw();
                float pitch = player.getPitch();
                boolean rotation = pitch != NoClip.lastPitch || yaw != NoClip.lastYaw;
                boolean position = !pos.equals(NoClip.lastPos) || idle;
                if (position || rotation) {
                    if (position) {
                        NoClip.timesSinceMoved = 0;
                        if (rotation) {
                            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, yaw, pitch, false));
                        } else {
                            this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, false));
                        }
                    } else {
                        this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, false));
                    }
                }
                NoClip.lastPos = pos;
                NoClip.lastYaw = yaw;
                NoClip.lastPitch = pitch;
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
        if (NoClip.isIgnoringWalls()) ci.cancel();
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void slowDown(CallbackInfoReturnable<Boolean> cir) {
        if (NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }

    @Inject(method = "shouldAutoJump", at = @At("HEAD"), cancellable = true)
    private void autoJump(CallbackInfoReturnable<Boolean> cir) {
        if (NoClip.isIgnoringWalls()) cir.setReturnValue(false);
    }

    @Redirect(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z", opcode = Opcodes.GETFIELD))
    private boolean canFly(PlayerAbilities instance) {
        if (InteractionManager.shouldTeleportUp()) return false;
        return instance.allowFlying;
    }
}
