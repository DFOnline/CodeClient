package dev.dfonline.codeclient.mixin.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public class MClientPlayerInteractionManager {
    @Shadow @Final private ClientPlayNetworkHandler networkHandler;

    @Shadow @Final private static Logger LOGGER;

    @Inject(
            method = "clickSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"),
            cancellable = true,
    )
    public void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (actionType == SlotActionType.CLONE) {
            ScreenHandler handler = player.currentScreenHandler;
            Slot slot = handler.getSlot(slotId);
            ItemStack original = slot.getStack().copy();

            LOGGER.info(handler.getCursorStack().toString());

            var manager = ((ClientPlayerInteractionManager)(Object)this);
            manager.clickSlot(syncId, slotId, 0, SlotActionType.SWAP, player);
            networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36, handler.getCursorStack()));
            manager.clickSlot(syncId, slotId, 0, SlotActionType.SWAP, player);
            manager.clickSlot(syncId, 54, 0, SlotActionType.QUICK_CRAFT, player);
            manager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, player);

//            networkHandler.sendPacket(new ClickSlotC2SPacket(
//                    syncId, screenHandler.getRevision(), slotId, button, actionType, screenHandler.getCursorStack().copy(), int2ObjectMap
//            ));
            ci.cancel();
        }
    }

}
