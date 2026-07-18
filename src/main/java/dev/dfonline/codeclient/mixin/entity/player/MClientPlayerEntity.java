package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.dev.Navigation;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
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

@Mixin(LocalPlayer.class)
public abstract class MClientPlayerEntity {
    @Shadow
    @Final
    public ClientPacketListener connection;

    @Shadow
    private boolean crouching;
    private boolean lastSneaking = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        var inst = FabricLoader.getInstance().getAllMods();
        CodeClient.onTick();
    }

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void sendMovementPackets(CallbackInfo ci) {
        if (CodeClient.location instanceof Dev plot) {
            if (CodeClient.currentAction instanceof MoveToSpawn mts) if (mts.moveModifier()) ci.cancel();
            LocalPlayer player = MC.player;
            if(player.isShiftKeyDown())  CodeClient.getFeature(Navigation.class).map(navigation -> navigation.onCrouch(lastSneaking));
            var NoClip = CodeClient.getFeature(NoClip.class).orElse(null);
            if (NoClip != null && NoClip.isIgnoringWalls()) {
                ci.cancel();
                Vec3 pos = NoClip.handleSeverPosition();
                boolean idle = NoClip.timesSinceMoved++ > 40;
                if (idle) NoClip.timesSinceMoved = 0;
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                boolean rotation = pitch != NoClip.lastPitch || yaw != NoClip.lastYaw;
                boolean position = !pos.equals(NoClip.lastPos) || idle;
                if (position || rotation) {
                    if (position) {
                        NoClip.timesSinceMoved = 0;
                        if (rotation) {
                            this.connection.send(new ServerboundMovePlayerPacket.PosRot(pos.x, pos.y, pos.z, yaw, pitch, false, true));
                        } else {
                            this.connection.send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, false, true));
                        }
                    } else {
                        this.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, false, true));
                    }
                }
                NoClip.lastPos = pos;
                NoClip.lastYaw = yaw;
                NoClip.lastPitch = pitch;
            }
            boolean sneaking = player.isShiftKeyDown();
            if (sneaking != this.lastSneaking) {
                Input mode = sneaking ? new Input(false, false, false, false, false, true, false) : MC.player.getLastSentInput();
                this.connection.send(new ServerboundPlayerInputPacket(mode));
                this.lastSneaking = sneaking;
            }
        }
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double z, CallbackInfo ci) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) ci.cancel();
    }

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void slowDown(CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) cir.setReturnValue(false);
    }

    @Inject(method = "canAutoJump", at = @At("HEAD"), cancellable = true)
    private void autoJump(CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.getFeature(NoClip.class).map(NoClip::isIgnoringWalls).orElse(false)) cir.setReturnValue(false);
    }

    @Redirect(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    private boolean canFly(Abilities instance) {
        if (CodeClient.getFeature(Navigation.class).map(Navigation::shouldTeleportUp).orElse(false)) return false;
        return instance.mayfly;
    }
}
