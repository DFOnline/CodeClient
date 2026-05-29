package dev.dfonline.codeclient.mixin.screen.chat;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.highlighter.ExpressionHighlighter;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.IntegerRange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ChatInputSuggestor.class)
public class MChatInputSuggester {

    @Shadow @Final
    TextFieldWidget textField;

    @Unique
    private OrderedText preview = null;
    @Inject(method = "provideRenderText", at = @At("RETURN"), cancellable = true)
    private void provideRenderText(String partial, int position, CallbackInfoReturnable<OrderedText> cir) {
        if (Objects.equals(partial, "")) return;
        CodeClient.getFeature(ExpressionHighlighter.class).ifPresent((action) -> {
            if (!action.enabled() || !(CodeClient.location instanceof Dev)) return;

            var range = IntegerRange.of(position, position + partial.length());
            ExpressionHighlighter.HighlightedExpression expression = action.format(textField.getText(), partial, range);

            if (expression == null) return;

            if (preview != OrderedText.empty()) preview = expression.preview();
            else preview = null;

            cir.setReturnValue(expression.text());
        });
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderPreview(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        CodeClient.getFeature(ExpressionHighlighter.class).ifPresent((action) -> {
            if (!action.enabled() || !(CodeClient.location instanceof Dev) || preview == null) return;

            action.draw(context, mouseX, mouseY, preview);
            preview = null; // prevents a preview from showing if the player deletes all text
        });
    }
}
