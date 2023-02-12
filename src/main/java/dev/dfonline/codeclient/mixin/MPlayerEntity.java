package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.NoClip;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MPlayerEntity {
    @Shadow public abstract boolean isLocalPlayer();

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void updatePose(CallbackInfo ci) {
        if(!this.isLocalPlayer()) return;
        if(NoClip.ignoresWalls()) ci.cancel();
        CodeClient.MC.player.setPose(Pose.STANDING);
    }
}
