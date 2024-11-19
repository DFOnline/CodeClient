package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class MCreativeInventoryScreen {
    @Shadow
    @Nullable
    private Slot deleteItemSlot;

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    public void slotClicked(@Nullable Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (CodeClient.location instanceof Dev dev
                && dev.isInDevSpace() // Clear the inventory regardless of mode if not in dev space
                && actionType == SlotActionType.QUICK_MOVE
                && slot == this.deleteItemSlot) {

            String cmd = Config.getConfig().DestroyItemResetMode.command;

            if (cmd != null) {
                CodeClient.MC.setScreen(null);
                CommandSender.queue(cmd);
                ci.cancel();
            }

        }
    }
}