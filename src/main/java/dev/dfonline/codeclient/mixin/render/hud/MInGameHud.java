package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.OverlayManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
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
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        if(OverlayManager.getOverlayText().size() == 0) return;
        TextRenderer textRenderer = getTextRenderer();
        int index = 0;
        List<Text> overlay = List.copyOf(OverlayManager.getOverlayText());
        for (Text text : overlay){
            context.drawTextWithShadow(textRenderer, text, 30, 30 + (index * 9), -1);
            index++;
        }
    }
}
