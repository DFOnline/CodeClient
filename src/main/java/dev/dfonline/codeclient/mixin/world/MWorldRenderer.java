package dev.dfonline.codeclient.mixin.world;

import dev.dfonline.codeclient.dev.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class MWorldRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private ClientLevel level;

    @Redirect(method = "extractBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean isAir(BlockState instance) {
        if (minecraft.hitResult instanceof BlockHitResult hitResult) {
            if (InteractionManager.customVoxelShape(level, hitResult.getBlockPos()) != null) return false;
        }
        return instance.isAir();
    }
}
