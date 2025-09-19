package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class SignOutlineRenderer extends Feature {

    public static BlockPos highlightedSign = null;

//    private static final RenderLayer.MultiPhase SIGN_OUTLINE = RenderLayer.of(
//            "codeclient:sign_outline",
//            VertexFormats.POSITION_COLOR,
//            VertexFormat.DrawMode.TRIANGLE_STRIP,
//            1536,
//            RenderLayer.MultiPhaseParameters.builder()
//                    .program(RenderLayer.POSITION_COLOR_PROGRAM)
//                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
//                    .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
//                    .(RenderLayer.TRANSLUCENT_TRANSPARENCY)
//                    .target(RenderLayer.OUTLINE_TARGET)
//                    .writeMaskState(RenderLayer.ALL_MASK)
//                    .cull(RenderLayer.DISABLE_CULLING)
//                    .build(true)
//    );

    private static final RenderLayer.MultiPhase SIGN_OUTLINE = RenderLayer.of(
            "codeclient:sign_outline",
            1536,
            false,
            true,
            RenderPipelines.ENTITY_OUTLINE_BLIT, // FIXME
            RenderLayer.MultiPhaseParameters.builder().build(RenderLayer.OutlineMode.IS_OUTLINE)
    );

    public static void render(SignBlockEntity sign, MatrixStack matrices, VertexConsumerProvider.Immediate immediate) {
        if (CodeClient.location instanceof Dev) {
            if (highlightedSign == null || !highlightedSign.equals(sign.getPos())) return;
            matrices.push();

            VoxelShape signShape = CodeClient.MC.world.getBlockState(sign.getPos()).getOutlineShape(CodeClient.MC.world, sign.getPos());
            VertexRendering.drawFilledBox(
                    matrices,
                    immediate.getBuffer(SIGN_OUTLINE),
                    signShape.getMin(Direction.Axis.X),
                    signShape.getMin(Direction.Axis.Y),
                    signShape.getMin(Direction.Axis.Z),
                    signShape.getMax(Direction.Axis.X),
                    signShape.getMax(Direction.Axis.Y),
                    signShape.getMax(Direction.Axis.Z),
                    ((Config.getConfig().SignHighlightColor >> 16) & 0xFF) / 255f,
                    ((Config.getConfig().SignHighlightColor >> 8) & 0xFF) / 255f,
                    (Config.getConfig().SignHighlightColor & 0xFF) / 255f,
                    1.0f
            );


            matrices.pop();
        }
    }
}
