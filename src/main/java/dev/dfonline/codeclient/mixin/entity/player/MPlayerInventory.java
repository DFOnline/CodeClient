package dev.dfonline.codeclient.mixin.entity.player;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.menu.RecentValues;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class MPlayerInventory {

    @Shadow @Final public Player player;

    @Inject(method = "setItem", at = @At("HEAD"))
    public void onSetStack(int slot, ItemStack item, CallbackInfo ci) {
        if (player == CodeClient.MC.player) CodeClient.getFeature(RecentValues.class)
                .ifPresent(recentValues -> recentValues.remember(item));
    }

}
