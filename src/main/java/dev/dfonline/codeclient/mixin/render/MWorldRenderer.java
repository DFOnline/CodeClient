package dev.dfonline.codeclient.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MWorldRenderer {

//    FIXME: signoutline getting more broken as we speak
//    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
//    public void renderMain(Camera camera, Frustum frustum, List<Entity> output, CallbackInfoReturnable<Boolean> cir) {
//        if (SignOutlineRenderer.highlightedSign != null) {
//            cir.setReturnValue(true);
//            cir.cancel();
//        }
//    }

    // TODO: use WorldRenderEvents?
    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    public void render(GpuBufferSlice gpuBufferSlice, LevelRenderState worldRenderState, ProfilerFiller profiler, Matrix4f matrix4f, ResourceHandle handle, ResourceHandle handle2, boolean bl, ResourceHandle handle3, ResourceHandle handle4, CallbackInfo ci, @Local(ordinal = 1) MultiBufferSource.BufferSource immediate, @Local PoseStack matrixStack) {
        Vec3 vec3d = worldRenderState.cameraRenderState.pos;
        CodeClient.onRender(matrixStack, immediate, vec3d.x, vec3d.y, vec3d.z);
    }
}
