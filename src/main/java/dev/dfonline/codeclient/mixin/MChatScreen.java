package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(ChatScreen.class)
public abstract class MChatScreen {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        if (CodeClient.MC.player == null || !(CodeClient.location instanceof Dev) || !Config.getConfig().ChatEditsVars) return;
        var item = VarItems.parse(CodeClient.MC.player.getMainHandStack());
        DecimalFormat format = new DecimalFormat("0.#");
        if(item instanceof Variable var) {
            this.chatField.setText(var.getName().replaceAll("§","&") + " -" + var.getScope().tag);
        }
        else if(item instanceof NamedItem named) {
            this.chatField.setText(named.getName().replaceAll("§","&"));
        }
        else if(item instanceof Vector vec) {
            this.chatField.setText("%s %s %s".formatted(format.format(vec.getX()), format.format(vec.getY()), format.format(vec.getZ())));
        }
        else if(item instanceof Sound sound) {
            this.chatField.setText("%s %s".formatted(format.format(sound.getPitch()), format.format(sound.getVolume())));
        }
        else return;
        this.chatField.setCursorToStart(false);
        this.chatField.setCursorToEnd(true);
    }
}
