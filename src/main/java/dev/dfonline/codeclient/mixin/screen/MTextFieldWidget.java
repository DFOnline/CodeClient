package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public class MTextFieldWidget {
    @Inject(method = "charTyped", at = @At("HEAD"))
    public void charTypes(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CodeClient.onCharTyped(chr, modifiers)) cir.setReturnValue(true);

    }
}
