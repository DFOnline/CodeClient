package dev.dfonline.codeclient.mixin.render;

import dev.dfonline.codeclient.dev.SignOutlineRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldRenderer.class)
public class MWorldRenderer {

//    FIXME: signoutline getting more broken as we speak
//    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
//    public void renderMain(Camera camera, Frustum frustum, List<Entity> output, CallbackInfoReturnable<Boolean> cir) {
//        if (SignOutlineRenderer.highlightedSign != null) {
//            cir.setReturnValue(true);
//            cir.cancel();
//        }
//    }

}
