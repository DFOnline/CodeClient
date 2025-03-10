package dev.dfonline.codeclient.mixin.render;

import com.google.common.collect.ImmutableMap;
import dev.dfonline.codeclient.dev.CodeSearcher;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static net.minecraft.client.render.block.entity.SignBlockEntityRenderer.createSignModel;


@Mixin(SignBlockEntityRenderer.class)
public abstract class MSignRenderer {



    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Shadow
    @Final
    private TextRenderer textRenderer;
    @Unique
    private Map<WoodType, Pair<Model, Model>> typeToModelPair;

    @Inject(method="<init>", at = @At("TAIL"))
    private void constructor(BlockEntityRendererFactory.Context ctx, CallbackInfo ci) {
        this.typeToModelPair = (Map<WoodType, Pair<Model, Model>>)WoodType.stream()
                .collect(
                        ImmutableMap.toImmutableMap(
                                signType -> signType,
                                signType -> new Pair<Model, Model>(
                                        // true, false
                                        createSignModel(ctx.getLayerRenderDispatcher(), signType, true), createSignModel(ctx.getLayerRenderDispatcher(), signType, false)
                                )
                        )
                );
    }


    @Shadow
    void render(SignBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BlockState state, AbstractSignBlock block, WoodType woodType, Model model) {}

    /**
     * @author PolytopeUtils
     * @reason yeh
     */
    @Overwrite
    public void render(SignBlockEntity signBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {

        BlockState blockState = signBlockEntity.getCachedState();
        AbstractSignBlock abstractSignBlock = (AbstractSignBlock)blockState.getBlock();
        WoodType woodType = AbstractSignBlock.getWoodType(abstractSignBlock);
        var signModelPair = this.typeToModelPair.get(woodType);
        Model model = blockState.getBlock() instanceof SignBlock ? signModelPair.getLeft() : signModelPair.getRight();

        boolean shouldBeHighlighted = CodeSearcher.shouldHighlight(signBlockEntity);



        this.render(signBlockEntity, matrixStack, vertexConsumerProvider, i, j, blockState, abstractSignBlock, woodType, model);

        VertexConsumerProvider newProvider = vertexConsumerProvider;
        if (!mc.player.isCreative() || !shouldBeHighlighted)  return;

        double distance = Math.sqrt(signBlockEntity.getPos().getSquaredDistance(mc.cameraEntity.getBlockPos()));

        double dist = MathHelper.clamp(distance, 1, 11);

        OutlineVertexConsumerProvider outlineVertexConsumerProvider = mc.getBufferBuilders().getOutlineVertexConsumers();
        outlineVertexConsumerProvider.setColor(220, 220, 220, (int) (dist * 5)); //256 - (int)(dist*17)
        newProvider = outlineVertexConsumerProvider;


        matrixStack.translate(0.5, 1f, 0);

        this.render(signBlockEntity, matrixStack, newProvider, i, j, blockState, abstractSignBlock, woodType, model);

    }




}
