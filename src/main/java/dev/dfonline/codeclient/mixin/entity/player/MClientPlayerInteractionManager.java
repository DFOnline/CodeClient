package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MClientPlayerInteractionManager {
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    public void onAttackBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlock", at = @At("HEAD") , cancellable = true)
    private void onBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(CodeClient.location instanceof Dev plot && plot.isInCodeSpace(hitResult.getBlockPos().getX(), hitResult.getPos().getZ()) && InteractionManager.onBlockInteract(player, hand, hitResult)) cir.setReturnValue(ActionResult.FAIL);
    }
    @Redirect(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    public void interactItemMovement(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if(!NoClip.ignoresWalls()) {
            instance.sendPacket(packet);
        }
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(slotId < 0) return;
        if(!player.isMainPlayer()) return;

        ScreenHandler screenHandler = player.currentScreenHandler;
        if(InteractionManager.onClickSlot(screenHandler.slots.get(slotId),button,actionType,syncId,screenHandler.getRevision())) ci.cancel();
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void reachDistance(CallbackInfoReturnable<Float> cir) {
        if(CodeClient.location instanceof Dev) {
            cir.setReturnValue(Config.getConfig().ReachDistance);
        }
    }
}
