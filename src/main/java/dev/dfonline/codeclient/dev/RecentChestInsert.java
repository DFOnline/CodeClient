package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
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
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if (CodeClient.location instanceof Dev) {
            if (lastChest == null) return;
            VoxelShape shape = CodeClient.MC.world.getBlockState(lastChest).getOutlineShape(CodeClient.MC.world, lastChest).offset(lastChest.getX(), lastChest.getY(), lastChest.getZ());
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            int color = Config.getConfig().ChestHighlightColor;
            float a = Math.min(alpha, 1f);

            color = (int) (a * 255) << 24 | (color & 0x00FFFFFF);
            VertexRendering.drawOutline(matrices, vertexConsumer, shape, -cameraX, -cameraY, -cameraZ, color);
        }
    }

    @Override
    public void tick() {
        if (lastChest != null) alpha -= 0.05F;
        if (alpha <= 0) lastChest = null;
    }

    @Override
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {
        if(CodeClient.MC.world.getBlockState(pos).getBlock() != Blocks.CHEST) return;
        if(Config.getConfig().HighlightChestsWithAir || !CodeClient.MC.player.getMainHandStack().isEmpty()) {
            setLastChest(pos);
        }
    }
}
