package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class RecentChestInsert {
    private static float alpha = 0;
    private static BlockPos lastChest = null;
    public static void setLastChest(BlockPos pos) {
        lastChest = pos;
        alpha = 10F;
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if(CodeClient.location instanceof Dev) {
            if(lastChest == null) return;
            VoxelShape shape = CodeClient.MC.world.getBlockState(lastChest).getOutlineShape(CodeClient.MC.world, lastChest).offset(lastChest.getX(),lastChest.getY(),lastChest.getZ());
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            Color color =  Config.getConfig().ChestHighlightColor;
            WorldRenderer.drawShapeOutline(matrices, vertexConsumer, shape, -cameraX, -cameraY, -cameraZ, (float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, Math.min(alpha,1), true);
        }
    }

    public static void tick() {
        if(lastChest != null) alpha -= 0.5;
        if(alpha <= 0) lastChest = null;
    }
}
