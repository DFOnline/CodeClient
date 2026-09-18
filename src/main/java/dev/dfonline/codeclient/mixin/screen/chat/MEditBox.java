package dev.dfonline.codeclient.mixin.screen.chat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EditBox.class)
public abstract class MEditBox {

    @Shadow
    private int displayPos;

    @Shadow
    private FormattedCharSequence applyFormat(String text, int offset) {
        throw new UnsupportedOperationException();
    }

    @Redirect(
            method = "extractWidgetRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I")
    )
    private int codeclient$getFormattedHighlightWidth(Font font, String str) {
        return font.width(applyFormat(str, displayPos));
    }

}
