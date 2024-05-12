package dev.dfonline.codeclient.mixin.render;

import dev.dfonline.codeclient.dev.SlotGhostManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MHandledScreen {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        SlotGhostManager.drawSlot(context,slot);
    }
}
