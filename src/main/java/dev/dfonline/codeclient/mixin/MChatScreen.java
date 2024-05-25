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

    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"))
    private void onOpen(CallbackInfo ci) {
        if (CodeClient.MC.player == null || !(CodeClient.location instanceof Dev) || !Config.getConfig().ChatEditsVars || !this.chatField.getText().isEmpty())
            return;
        var item = VarItems.parse(CodeClient.MC.player.getMainHandStack());
        DecimalFormat format = new DecimalFormat("0.#");
        if (item instanceof NamedItem named) {
            this.chatField.setText(named.getName().replaceAll("§", "&"));
        } else if (item instanceof Vector vec) {
            this.chatField.setText("%s %s %s".formatted(vec.getX(), vec.getY(), vec.getZ()));
        } else if (item instanceof Sound sound) {
            if (sound.getVolume() != 2)
                this.chatField.setText("%s %s".formatted(sound.getPitch(), format.format(sound.getVolume())));
            else this.chatField.setText(format.format(sound.getPitch()));
        } else if (item instanceof Potion pot) {
            if(pot.getDuration() == Potion.INFINITE) this.chatField.setText(Integer.toString(pot.getAmplifier() + 1));
            else this.chatField.setText("%o %s".formatted(pot.getAmplifier() + 1, pot.duration().replaceAll(" ticks","")));
        }
        else return;
        this.chatField.setCursorToStart(false);
        this.chatField.setCursorToEnd(true);
    }
}
