package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.CommandSender;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MCreativeInventoryScreen {
    @Shadow
    @Nullable
    private Slot destroyItemSlot;

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void slotClicked(@Nullable Slot slot, int slotId, int button, ContainerInput containerInput, CallbackInfo ci) {
        if (
                CodeClient.location instanceof Dev dev
                && dev.isInDevSpace() // Clear the inventory regardless of mode if not in dev space
                && containerInput == ContainerInput.QUICK_MOVE
                && slot == this.destroyItemSlot
        ) {
            String cmd = Config.getConfig().DestroyItemResetMode.command;

            if (cmd != null) {
                CodeClient.MC.setScreen(null);
                CommandSender.queue(cmd);
                ci.cancel();
            }

        }
    }
}