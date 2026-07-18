package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.BlockBreakDeltaCalculator;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MClientPlayerInteractionManager {
    private static ItemStack item = null;
    @Shadow
    private boolean isDestroying;
    @Shadow
    private BlockPos destroyBlockPos;
    @Shadow
    private float destroyProgress;
    @Shadow
    private float destroyTicks;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int destroyDelay;
    @Shadow
    private int carriedIndex;

    @Shadow
    protected abstract void startPrediction(ClientLevel world, PredictiveAction packetCreator);

    @Shadow
    public abstract int getDestroyStage();

    @Shadow
    public abstract boolean destroyBlock(BlockPos pos);

    @Shadow
    public abstract void stopDestroyBlock();

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (InteractionManager.onBreakBlock(pos)) cir.setReturnValue(false);
    }

    @ModifyVariable(method = "useItemOn", at = @At("HEAD"), argsOnly = true)
    private BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        return InteractionManager.onBlockInteract(hitResult);
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", shift = At.Shift.AFTER))
    public void afterSendPlace(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (item != null) {
            Utility.sendHandItem(item);
            item = null;
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    public void interactItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (InteractionManager.onItemInteract(player, hand)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.getConfig().CustomBlockBreaking) return;
        if (CodeClient.location instanceof Dev dev) {
            if (Utility.isGlitchStick(CodeClient.MC.player.getMainHandItem())) return;
            if (!dev.isInDev(pos)) return;
            if (CodeClient.MC.level.getBlockState(pos).getBlock() == Blocks.CHEST) return;
            BlockPos breakPos = InteractionManager.isBlockBreakable(pos);
            if (breakPos == null) {
                return;
            }
            this.isDestroying = true;
            this.destroyBlockPos = breakPos;
            this.destroyProgress = 0.0F;
            this.destroyTicks = 0.0F;
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.getConfig().CustomBlockBreaking) return;
        if (CodeClient.location instanceof Dev dev) {
            if (Utility.isGlitchStick(CodeClient.MC.player.getMainHandItem())) return;
            if (!dev.isInDev(pos)) return;
            if (CodeClient.MC.level.getBlockState(pos).getBlock() == Blocks.CHEST) return;
            cir.cancel();
            BlockPos breakPos = InteractionManager.isBlockBreakable(pos);
            if (breakPos == null || !breakPos.equals(destroyBlockPos) || !isDestroying) {
                stopDestroyBlock();
                CodeClient.MC.gameMode.startDestroyBlock(pos, direction);
                return;
            }
            CodeClient.getFeature(BlockBreakDeltaCalculator.class)
                    .ifPresent(feat -> this.destroyProgress += feat.calculateBlockDelta(breakPos));
            if (this.destroyProgress >= 1.0F) {
                this.isDestroying = false;
                this.startPrediction(this.minecraft.level, (sequence) -> {
                    this.destroyBlock(pos);
                    return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
                });
                this.destroyProgress = 0.0F;
                this.destroyTicks = 0.0F;
                this.destroyDelay = 5;
            }

            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
            cir.setReturnValue(true);
        }
    }
}
