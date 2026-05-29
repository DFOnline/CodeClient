package dev.dfonline.codeclient.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    // TODO: use WorldRenderEvents?
    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
    public void render(GpuBufferSlice gpuBufferSlice, WorldRenderState worldRenderState, Profiler profiler, Matrix4f matrix4f, Handle handle, Handle handle2, boolean bl, Handle handle3, Handle handle4, CallbackInfo ci, @Local(ordinal = 1) VertexConsumerProvider.Immediate immediate, @Local MatrixStack matrixStack) {
        Vec3d vec3d = worldRenderState.cameraRenderState.pos;
        CodeClient.onRender(matrixStack, immediate, vec3d.x, vec3d.y, vec3d.z);
    }
}
