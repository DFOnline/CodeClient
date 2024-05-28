package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.dev.InsertOverlay;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MMouse {
    @Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"))
    private boolean mouseScrolled(Screen instance, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return InsertOverlay.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount) || instance.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
    }
}
