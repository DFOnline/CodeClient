package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.InteractionManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class MHandledScreen {
    @Shadow protected int leftPos;

    @Shadow protected int topPos;

    @Shadow @Final protected AbstractContainerMenu menu;

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void drawSlot(GuiGraphics context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        CodeClient.onDrawSlot(context,slot);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        CodeClient.onScreenInit((AbstractContainerScreen<?>) (Object) this);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    public void removed(CallbackInfo ci) {
        CodeClient.onScreenClosed();
    }

    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    private void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CodeClient.onRender(context,mouseX,mouseY,this.leftPos,this.topPos,delta);
    }

    @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"))
    private boolean hasStack(Slot instance) {
        ItemStack hover = CodeClient.onGetHoverStack(instance);
        return (hover != null && !hover.isEmpty()) || instance.hasItem();
    }

    @Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack getStack(Slot instance) {
        ItemStack hover = CodeClient.onGetHoverStack(instance);
        return hover == null || hover.isEmpty() ? instance.getItem() : hover;
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"), cancellable = true)
    private void clickSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (slotId < 0) return;

        if (InteractionManager.onClickSlot(slot,button,actionType,this.menu.containerId,this.menu.getStateId()))
            ci.cancel();
    }
}
