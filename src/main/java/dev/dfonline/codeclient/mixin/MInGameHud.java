package dev.dfonline.codeclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dfonline.codeclient.OverlayManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Gui.class)
public abstract class MInGameHud {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if(OverlayManager.getOverlayText().size() == 0) return;
        Font font = getFont();
        int index = 0;
        List<Component> overlay = List.copyOf(OverlayManager.getOverlayText());
        for (Component text : overlay){
            font.drawShadow(poseStack, text, 30, 30 + (index * 9), -1);
            index++;
        }
    }
}
