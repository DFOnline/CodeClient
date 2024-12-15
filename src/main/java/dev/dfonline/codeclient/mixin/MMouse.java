package dev.dfonline.codeclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.BuildPhaser;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MMouse {
    @WrapOperation(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"))
    private boolean mouseScrolled(Screen instance, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        return CodeClient.onMouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount)
                || original.call(instance, mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @WrapOperation(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
    public boolean spectator(ClientPlayerEntity instance, Operation<Boolean> original) {
        var feat = CodeClient.getFeature(BuildPhaser.class);
        if (feat.isPresent() && feat.get().isClipping()) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
