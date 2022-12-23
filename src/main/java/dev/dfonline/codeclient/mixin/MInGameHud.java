package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.OverlayManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public abstract class MInGameHud {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if(OverlayManager.getOverlayText().size() == 0) return;
        TextRenderer textRenderer = getTextRenderer();
        int index = 0;
        for (Text text : OverlayManager.getOverlayText()){
            textRenderer.drawWithShadow(matrices, text, 30, 30 + (index * 9), -1);
            index++;
        }
    }
}
