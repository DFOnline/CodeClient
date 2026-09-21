package dev.dfonline.codeclient.mixin.screen.chat;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.ChatAutoEdit;
import dev.dfonline.codeclient.dev.ChatLongValue;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MChatScreen {

    @Shadow
    protected EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        CodeClient.getFeature(ChatLongValue.class).ifPresent(chatLongValue -> chatLongValue.onOpenChat(input));
        CodeClient.getFeature(ChatAutoEdit.class).ifPresent(chatAutoEdit -> chatAutoEdit.onOpenChat(input));
    }

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void onSend(String chatText, boolean addToHistory, CallbackInfo ci) {
        CodeClient.getFeature(ChatLongValue.class).ifPresent(chatLongValue -> {
            if (chatLongValue.onSendChat(chatText)) {
                ci.cancel();
            }
        });
    }
}
