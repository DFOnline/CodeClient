package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.RecentValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class MPlayerInventory {

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "setStack", at = @At("HEAD"))
    public void onSetStack(int slot, ItemStack item, CallbackInfo ci) {
        if (player == CodeClient.MC.player) RecentValues.remember(item);
    }

}
