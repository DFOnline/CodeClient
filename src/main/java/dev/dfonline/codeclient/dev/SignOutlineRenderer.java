package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

import java.util.OptionalDouble;

public class SignOutlineRenderer extends Feature {

    public static BlockPos highlightedSign = null;

    private static final RenderLayer.MultiPhase ESP_LINE = RenderLayer.of(
            "codeclient:esp_lines",
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .target(RenderLayer.ITEM_ENTITY_TARGET)
                    .writeMaskState(RenderLayer.COLOR_MASK)
                    .depthTest(RenderLayer.ALWAYS_DEPTH_TEST)
                    .cull(RenderLayer.DISABLE_CULLING)
                    .build(false)
    );

    public static void render(SignBlockEntity sign, MatrixStack matrices, VertexConsumerProvider.Immediate immediate) {
        if(CodeClient.location instanceof Dev) {
            if (highlightedSign == null || !highlightedSign.equals(sign.getPos())) return;
            matrices.push();

            VoxelShape signShape = CodeClient.MC.world.getBlockState(sign.getPos()).getOutlineShape(CodeClient.MC.world, sign.getPos());

            VertexConsumer consumer = immediate.getBuffer(ESP_LINE);
            VertexRendering.drawOutline(matrices, consumer, signShape, 0, 0, 0, Config.getConfig().SignHighlightColor);
            immediate.draw(ESP_LINE);

            matrices.pop();
        }
    }
}
