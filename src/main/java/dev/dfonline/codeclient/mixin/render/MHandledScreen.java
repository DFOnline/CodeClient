package dev.dfonline.codeclient.mixin.render;

import dev.dfonline.codeclient.dev.SlotGhostManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class MHandledScreen {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        SlotGhostManager.drawSlot(context,slot);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlotGhostManager.render(context,mouseX,mouseY,(HandledScreen<?>) (Object) this);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(SlotGhostManager.mouseClicked(mouseX,mouseY,button, (HandledScreen<?>) (Object) this)) cir.setReturnValue(true);
    }
}
