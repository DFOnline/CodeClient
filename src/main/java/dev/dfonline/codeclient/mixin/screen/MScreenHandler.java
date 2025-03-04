package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.menu.AdvancedMiddleClickFeature;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class MScreenHandler {

    @Shadow public abstract ItemStack getCursorStack();

    @Shadow @Final public DefaultedList<Slot> slots;

    @Shadow public abstract void setCursorStack(ItemStack stack);

    @Inject(
            method = "onSlotClick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void clickSlot(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        // creative inventories act differently, and don't keep track of the item in the cursor, and will trust the client.
        if (CodeClient.location instanceof Dev &&  actionType == SlotActionType.CLONE) {
            if (!AdvancedMiddleClickFeature.activated()) return;
            if (player.isInCreativeMode()) {
                var slot = (Slot) slots.get(slotIndex);
                var clone = AdvancedMiddleClickFeature.getCopy(slot, getCursorStack());
                if (clone != null) {
                    setCursorStack(clone);
                }
                ci.cancel();
            }
        }
    }
}
