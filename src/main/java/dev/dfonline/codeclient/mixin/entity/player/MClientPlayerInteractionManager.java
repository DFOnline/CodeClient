package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.NoClip;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MClientPlayerInteractionManager {
    private static ItemStack item = null;

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    public void onAttackBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @ModifyVariable(method = "interactBlock", at = @At("HEAD"), argsOnly = true)
    private BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        if(CodeClient.location instanceof Dev plot && plot.isInCodeSpace(hitResult.getBlockPos().getX(), hitResult.getPos().getZ())) {
            return InteractionManager.onBlockInteract(hitResult);
        }
        return hitResult;
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"))
    public void beforeSendPlace(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack handItem = player.getStackInHand(hand);
        boolean isTemplate = handItem.hasNbt() && handItem.getNbt() != null && handItem.getNbt().contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE) && handItem.getNbt().getCompound("PublicBukkitValues").contains("hypercube:codetemplatedata", NbtElement.STRING_TYPE);
        if(!isTemplate) return;
        ItemStack template = Items.ENDER_CHEST.getDefaultStack();
        template.setNbt(handItem.getNbt());
        Utility.sendHandItem(template);
        item = handItem;
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", shift = At.Shift.AFTER))
    public void afterSendPlace(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(item != null) {
            Utility.sendHandItem(item);
            item = null;
        }
    }

    @Redirect(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    public void interactItemMovement(ClientPlayNetworkHandler instance, Packet<?> packet) {
        if(!NoClip.isIgnoringWalls()) {
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
