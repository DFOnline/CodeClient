package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public class MInGameOverlayRenderer {
    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
    private static void inWall(Player player, CallbackInfoReturnable<BlockState> cir) {
        if(NoClip.ignoresWalls()) cir.setReturnValue(null);
    }
}
