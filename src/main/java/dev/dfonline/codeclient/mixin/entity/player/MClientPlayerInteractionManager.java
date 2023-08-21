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
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MClientPlayerInteractionManager {
    @Shadow protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    private static ItemStack item = null;

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    public void onAttackBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"))
    public void beforeSendPlace(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack handItem = player.getStackInHand(hand);
        boolean isTemplate = handItem.hasNbt() && handItem.getNbt() != null && handItem.getNbt().contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE) && handItem.getNbt().getCompound("PublicBukkitValues").contains("hypercube:codetemplatedata", NbtElement.STRING_TYPE);
        if(!isTemplate) return;
        BlockPos place = InteractionManager.getPlacePos(hitResult);
        if(place != null && CodeClient.MC.world.getBlockState(place).isSolidBlock(CodeClient.MC.world, place)) {
            ClientPlayNetworkHandler net = CodeClient.MC.getNetworkHandler();
            if(!player.isSneaking()) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            this.sendSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));
            if(!player.isSneaking()) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            if(!player.isSneaking()) {
                this.sendSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(place.toCenterPos(), Direction.UP, place, hitResult.isInsideBlock()), sequence));
            }
        }
        ItemStack template = Items.ENDER_CHEST.getDefaultStack();
        template.setNbt(handItem.getNbt());
        Utility.sendHandItem(template);
        item = handItem;
    }

    @ModifyVariable(method = "interactBlock", at = @At("HEAD"), argsOnly = true)
    private BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        if(CodeClient.location instanceof Dev plot && plot.isInCodeSpace(hitResult.getBlockPos().getX(), hitResult.getPos().getZ())) {
            return InteractionManager.onBlockInteract(hitResult);
        }
        return hitResult;
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

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    public void interactItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if(InteractionManager.onItemInteract(player, hand)) {
            cir.setReturnValue(ActionResult.PASS);
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
