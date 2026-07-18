package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.ValueDetails;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class MDrawContext {

    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    public abstract void drawString(Font textRenderer, Component text, int x, int y, int color, boolean shadow);

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    private void additionalItemRendering(Font textRenderer, ItemStack stack, int x, int y, String stackCountText, CallbackInfo ci) {
        CodeClient.getFeature(ValueDetails.class).ifPresent(valueDetails ->
                valueDetails.draw(this::drawString, textRenderer, stack, x, y, pose));
    }
}
