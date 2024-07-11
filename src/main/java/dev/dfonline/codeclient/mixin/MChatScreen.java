package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.ChatAutoEdit;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MChatScreen {

    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        CodeClient.getFeature(ChatAutoEdit.class).ifPresent(chatAutoEdit -> chatAutoEdit.onOpenChat(chatField));
    }
}
