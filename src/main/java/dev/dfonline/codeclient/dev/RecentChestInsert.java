package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class RecentChestInsert {
    private static float alpha = 0;
    private static BlockPos lastChest = null;
    public static void setLastChest(BlockPos pos) {
        lastChest = pos;
        alpha = 10F;
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if(CodeClient.location instanceof Dev plot) {
            if(lastChest == null) return;
            VoxelShape shape = CodeClient.MC.world.getBlockState(lastChest).getOutlineShape(CodeClient.MC.world, lastChest).offset(lastChest.getX(),lastChest.getY(),lastChest.getZ());
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            WorldRenderer.drawShapeOutline(matrices, vertexConsumer, shape, -cameraX, -cameraY, -cameraZ, 0.2F, 1.0F, 1.0F, Math.min(alpha,1), true);
        }
    }

    public static void tick() {
        if(lastChest != null) alpha -= 2;
        if(alpha <= 0) lastChest = null;
    }
}
