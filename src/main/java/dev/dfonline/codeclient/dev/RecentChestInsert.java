package dev.dfonline.codeclient.dev;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecentChestInsert extends Feature {
    private float alpha = 0;
    private BlockPos lastChest = null;

    public void setLastChest(BlockPos pos) {
        lastChest = pos;
        alpha = Config.getConfig().HighlightChestDuration;
    }

    @Override
    public boolean enabled() {
        return Config.getConfig().RecentChestInsert;
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if (CodeClient.location instanceof Dev) {
            if (lastChest == null) return;
            VoxelShape shape = CodeClient.MC.level.getBlockState(lastChest).getShape(CodeClient.MC.level, lastChest).move(lastChest.getX(), lastChest.getY(), lastChest.getZ());
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderTypes.LINES);
            int color = Config.getConfig().ChestHighlightColor;
            float a = Math.min(alpha, 1f);

            color = (int) (a * 255) << 24 | (color & 0x00FFFFFF);
            ShapeRenderer.renderShape(matrices, vertexConsumer, shape, -cameraX, -cameraY, -cameraZ, color, CodeClient.MC.getWindow().getAppropriateLineWidth());
        }
    }

    @Override
    public void tick() {
        if (lastChest != null) alpha -= 0.05F;
        if (alpha <= 0) lastChest = null;
    }

    @Override
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {
        if(CodeClient.MC.level.getBlockState(pos).getBlock() != Blocks.CHEST) return;
        if(Config.getConfig().HighlightChestsWithAir || !CodeClient.MC.player.getMainHandItem().isEmpty()) {
            setLastChest(pos);
        }
    }
}
