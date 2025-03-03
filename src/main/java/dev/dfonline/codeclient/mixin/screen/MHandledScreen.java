package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
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

@Mixin(HandledScreen.class)
public abstract class MHandledScreen {
    @Shadow protected int x;

    @Shadow protected int y;

    @Shadow @Final protected ScreenHandler handler;

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        CodeClient.onDrawSlot(context,slot);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        CodeClient.onScreenInit((HandledScreen<?>) (Object) this);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    public void removed(CallbackInfo ci) {
        CodeClient.onScreenClosed();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CodeClient.onRender(context,mouseX,mouseY,this.x,this.y,delta);
    }

    @Redirect(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z"))
    private boolean hasStack(Slot instance) {
        ItemStack hover = CodeClient.onGetHoverStack(instance);
        return (hover != null && !hover.isEmpty()) || instance.hasStack();
    }

    @Redirect(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack getStack(Slot instance) {
        ItemStack hover = CodeClient.onGetHoverStack(instance);
        return hover == null || hover.isEmpty() ? instance.getStack() : hover;
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void clickSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slotId < 0) return;

        if (InteractionManager.onClickSlot(slot,button,actionType,this.handler.syncId,this.handler.getRevision())) {
            ci.cancel();
        }
    }
}
