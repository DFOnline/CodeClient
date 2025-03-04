package dev.dfonline.codeclient.mixin.network;

import dev.dfonline.codeclient.Utility;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerInteractionManager;
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
            cancellable = true
    )
    public void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        LOGGER.info(String.valueOf(slotId));
        if (actionType == SlotActionType.CLONE) {
            ScreenHandler handler = player.currentScreenHandler;

            var manager = ((ClientPlayerInteractionManager)(Object)this);

            var emptySlot = player.getInventory().getEmptySlot();
            var stack = handler.getCursorStack().copy();

            LOGGER.info(String.valueOf(emptySlot));
            networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
                    Utility.getRemoteSlot(emptySlot), stack)
            );
            handler.setCursorStack(stack);

            int convertedSlotId = Utility.getRemoteSlot(emptySlot)-9+(handler.slots.size()-36);
            Slot slot = handler.getSlot(convertedSlotId);

            manager.clickSlot(syncId, convertedSlotId, 0, SlotActionType.PICKUP, player);
            slot.getStack().setCount(0);

            ci.cancel();
        }
    }

}
