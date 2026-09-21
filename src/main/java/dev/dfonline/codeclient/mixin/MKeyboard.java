package dev.dfonline.codeclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.switcher.SpeedSwitcher;
import dev.dfonline.codeclient.switcher.StateSwitcher;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class MKeyboard {
    @Unique
    private static final int DEBUG_KEY = GLFW.GLFW_KEY_F3;
    @Unique
    private static final int stateSwitcherKey = GLFW.GLFW_KEY_F4;
    @Unique
    private static final int speedSwitcherKey = GLFW.GLFW_KEY_F5;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void handleF3(KeyEvent key, CallbackInfoReturnable<Boolean> cir) {
        if (key.input() == stateSwitcherKey) {
            if (
                    CodeClient.getFeature(StateSwitcher.StateSwitcherFeature.class)
                            .map(StateSwitcher.StateSwitcherFeature::open)
                            .orElse(false)) cir.setReturnValue(true);
        }
        if (key.input() == speedSwitcherKey) {
            if(
                    CodeClient.getFeature(SpeedSwitcher.SpeedSwitcherFeature.class)
                            .map(SpeedSwitcher.SpeedSwitcherFeature::open)
                            .orElse(false)) cir.setReturnValue(false);
        }
    }

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (KeyBinds.previewItemTags.matches(input)) {
            // 1 = press, 2 = repeat, 0 = release
            CodeClient.isPreviewingItemTags = action == 1 || action == 2;
        }
    }

    @WrapOperation(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"))
    private boolean onChar(Screen instance, CharacterEvent charInput, Operation<Boolean> original) {
        return CodeClient.onCharTyped(charInput) || original.call(instance, charInput);
    }
}
