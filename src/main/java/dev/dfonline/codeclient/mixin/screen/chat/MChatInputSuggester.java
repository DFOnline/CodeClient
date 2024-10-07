package dev.dfonline.codeclient.mixin.screen.chat;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.highlighter.ExpressionHighlighter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatInputSuggestor.class)
public class MChatInputSuggester {

    @Shadow @Final
    TextFieldWidget textField;

    @Unique
    private OrderedText preview = null;

    @Inject(method = "provideRenderText", at = @At("RETURN"), cancellable = true)
    private void provideRenderText(String original, int firstCharacterIndex, CallbackInfoReturnable<OrderedText> cir) {
        OrderedText originalValue = cir.getReturnValue();

        CodeClient.getFeature(ExpressionHighlighter.class).ifPresent((action) -> {
            if (!action.enabled()) return;

            ExpressionHighlighter.HighlightedExpression expression = action.format(textField.getText());

            preview = expression.preview();
            cir.setReturnValue(expression.text()); // todo: highlighting in the edit box
        });
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderPreview(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        CodeClient.getFeature(ExpressionHighlighter.class).ifPresent((action) -> {
            if (!action.enabled() || preview == null) return;

            action.draw(context, mouseX, mouseY, preview);
        });
    }



}
