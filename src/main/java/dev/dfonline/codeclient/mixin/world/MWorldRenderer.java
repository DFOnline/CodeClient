package dev.dfonline.codeclient.mixin.world;

import dev.dfonline.codeclient.dev.InteractionManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class MWorldRenderer {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Nullable
    private ClientWorld world;

    @Redirect(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
    private boolean isAir(BlockState instance) {
        if (client.crosshairTarget instanceof BlockHitResult hitResult) {
            if (InteractionManager.customVoxelShape(world, hitResult.getBlockPos()) != null) return false;
        }
        return instance.isAir();
    }
}
