package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.menu.AdvancedMiddleClickFeature;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class MScreenHandler {

    @Shadow public abstract ItemStack getCarried();

    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract void setCarried(ItemStack stack);

    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    public void clickSlot(int slotIndex, int button, ContainerInput containerInput, Player player, CallbackInfo ci) {
        // creative inventories act differently, and don't keep track of the item in the cursor, and will trust the client.
        if (CodeClient.location instanceof Dev &&  containerInput == ContainerInput.CLONE) {
            if (!AdvancedMiddleClickFeature.activated()) return;
            if (player.hasInfiniteMaterials()) {
                var slot = (Slot) slots.get(slotIndex);
                var clone = AdvancedMiddleClickFeature.getCopy(slot, getCarried(), containerInput);
                if (clone != null) {
                    setCarried(clone);
                }
                ci.cancel();
            }
        }
    }
}
