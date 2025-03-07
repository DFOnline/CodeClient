package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class SignOutlineRenderer extends Feature {

    public static BlockPos highlightedSign = null;

    public static void render(SignBlockEntity sign, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if(CodeClient.location instanceof Dev) {
            if (highlightedSign == null || !highlightedSign.equals(sign.getPos())) return;
            matrices.push();

            VoxelShape signShape = CodeClient.MC.world.getBlockState(sign.getPos()).getOutlineShape(CodeClient.MC.world, sign.getPos());
            VertexRendering.drawOutline(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), signShape, 0, 0, 0, Config.getConfig().SignHighlightColor);

            matrices.pop();
        }
    }
}
