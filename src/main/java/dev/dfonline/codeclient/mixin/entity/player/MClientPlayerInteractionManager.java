package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.BlockBreakDeltaCalculator;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MClientPlayerInteractionManager {
    private static ItemStack item = null;
    @Shadow
    private boolean breakingBlock;
    @Shadow
    private BlockPos currentBreakingPos;
    @Shadow
    private float currentBreakingProgress;
    @Shadow
    private float blockBreakingSoundCooldown;
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int blockBreakingCooldown;
    @Shadow
    private int lastSelectedSlot;

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Shadow
    public abstract int getBlockBreakingProgress();

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Shadow
    public abstract void cancelBlockBreaking();

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    public void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"))
    public void beforeSendPlace(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack handItem = player.getStackInHand(hand);
        boolean isTemplate = DFItem.of(handItem).hasHypercubeKey("codetemplatedata");
        if (!isTemplate) return;
        BlockPos place = InteractionManager.getPlacePos(hitResult);
        if (place != null && CodeClient.MC.world.getBlockState(place).isSolidBlock(CodeClient.MC.world, place)) {
            ClientPlayNetworkHandler net = CodeClient.MC.getNetworkHandler();
            if (!player.isSneaking())
                net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            this.sendSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, sequence));
            if (!player.isSneaking())
                net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            if (!player.isSneaking()) {
                this.sendSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(place.toCenterPos(), Direction.UP, place, hitResult.isInsideBlock()), sequence));
            }
        }
        ItemStack template = Items.ENDER_CHEST.getDefaultStack();
        DFItem dfTemplate = DFItem.of(template);
        dfTemplate.setItemData(DFItem.of(handItem).getItemData());
        Utility.sendHandItem(dfTemplate.getItemStack());
        item = handItem;
    }

    @ModifyVariable(method = "interactBlock", at = @At("HEAD"), argsOnly = true)
    private BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        return InteractionManager.onBlockInteract(hitResult);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", shift = At.Shift.AFTER))
    public void afterSendPlace(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (item != null) {
            Utility.sendHandItem(item);
            item = null;
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    public void interactItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (InteractionManager.onItemInteract(player, hand)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.getConfig().CustomBlockBreaking) return;
        if (CodeClient.location instanceof Dev dev) {
            if (Utility.isGlitchStick(CodeClient.MC.player.getMainHandStack())) return;
            if (!dev.isInDev(pos)) return;
            if (CodeClient.MC.world.getBlockState(pos).getBlock() == Blocks.CHEST) return;
            BlockPos breakPos = InteractionManager.isBlockBreakable(pos);
            if (breakPos == null) {
                return;
            }
            this.breakingBlock = true;
            this.currentBreakingPos = breakPos;
            this.currentBreakingProgress = 0.0F;
            this.blockBreakingSoundCooldown = 0.0F;
            this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, this.getBlockBreakingProgress());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.getConfig().CustomBlockBreaking) return;
        if (CodeClient.location instanceof Dev dev) {
            if (Utility.isGlitchStick(CodeClient.MC.player.getMainHandStack())) return;
            if (!dev.isInDev(pos)) return;
            if (CodeClient.MC.world.getBlockState(pos).getBlock() == Blocks.CHEST) return;
            cir.cancel();
            BlockPos breakPos = InteractionManager.isBlockBreakable(pos);
            if (breakPos == null || !breakPos.equals(currentBreakingPos) || !breakingBlock) {
                cancelBlockBreaking();
                CodeClient.MC.interactionManager.attackBlock(pos, direction);
                return;
            }
            CodeClient.getFeature(BlockBreakDeltaCalculator.class)
                    .ifPresent(feat -> this.currentBreakingProgress += feat.calculateBlockDelta(breakPos));
            if (this.currentBreakingProgress >= 1.0F) {
                this.breakingBlock = false;
                this.sendSequencedPacket(this.client.world, (sequence) -> {
                    this.breakBlock(pos);
                    return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
                });
                this.currentBreakingProgress = 0.0F;
                this.blockBreakingSoundCooldown = 0.0F;
                this.blockBreakingCooldown = 5;
            }

            this.client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, this.getBlockBreakingProgress());
            cir.setReturnValue(true);
        }
    }
}
