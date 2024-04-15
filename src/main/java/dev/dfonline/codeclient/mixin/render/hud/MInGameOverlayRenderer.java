package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameOverlayRenderer.class)
public class MInGameOverlayRenderer {
    @Inject(method = "getInWallBlockState", at = @At("HEAD"), cancellable = true)
    private static void inWall(PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (NoClip.isIgnoringWalls()) cir.setReturnValue(null);
    }
}
