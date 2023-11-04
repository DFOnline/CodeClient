package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.overlay.ChestPeeker;
import dev.dfonline.codeclient.dev.overlay.SignPeeker;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
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

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        TextRenderer textRenderer = getTextRenderer();

        List<Text> overlay = List.copyOf(OverlayManager.getOverlayText());
        if(!overlay.isEmpty()) {
            int index = 0;
            for (Text text : overlay) {
                context.drawTextWithShadow(textRenderer, text, 30, 30 + (index * 9), -1);
                index++;
            }
        }
        List<Text> peeker = ChestPeeker.getOverlayText();
        if(peeker == null) peeker = SignPeeker.getOverlayText();
        if(peeker != null && !peeker.isEmpty()) {
            int x = (scaledWidth / 2) + Config.getConfig().ChestPeekerX;
            int yOrig = (scaledHeight / 2) + Config.getConfig().ChestPeekerY;
            context.drawTooltip(textRenderer,peeker,x,yOrig);

        }
    }
}
