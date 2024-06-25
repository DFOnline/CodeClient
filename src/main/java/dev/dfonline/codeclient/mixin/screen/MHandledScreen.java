package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.dev.InsertOverlay;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.SlotGhostManager;
import dev.dfonline.codeclient.dev.overlay.ActionViewer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class MHandledScreen {
    @Shadow protected int x;

    @Shadow protected int y;

    @Shadow @Final protected ScreenHandler handler;

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        SlotGhostManager.drawSlot(context,slot);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlotGhostManager.render(delta);
        InsertOverlay.render(context,mouseX,mouseY,(HandledScreen<?>) (Object) this, this.x, this.y);
        ActionViewer.render(context, mouseX, mouseY,(HandledScreen<?>) (Object) this);
    }

    @Redirect(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z"))
    private boolean hasStack(Slot instance) {
        var hover = SlotGhostManager.getHoverItem(instance);
        return (hover != null && !hover.isEmpty()) || instance.hasStack();
    }

    @Redirect(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack getStack(Slot instance) {
        var hover = SlotGhostManager.getHoverItem(instance);
        return hover == null || hover.isEmpty() ? instance.getStack() : hover;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(InsertOverlay.mouseClicked(mouseX,mouseY,button, (HandledScreen<?>) (Object) this)) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if(InsertOverlay.keyPressed(keyCode,scanCode,modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void clickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slotId < 0) return;

        if (InteractionManager.onClickSlot(slot,button,actionType,this.handler.syncId,this.handler.getRevision()))
            ci.cancel();
    }

}
