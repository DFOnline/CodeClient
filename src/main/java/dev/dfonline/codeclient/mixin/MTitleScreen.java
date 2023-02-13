package dev.dfonline.codeclient.mixin;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MTitleScreen {
    @Inject(method = "init", at = @At("RETURN"))
    protected void init(CallbackInfo ci) {
        if(CodeClient.justStarted) {
            CodeClient.justStarted = false;
            ServerData serverData = new ServerData("diamondfire", "mcdiamondfire.com", false);
            ConnectScreen.startConnecting((TitleScreen)(Object)this, CodeClient.MC, ServerAddress.parseString(serverData.ip), serverData);
        }
    }
}
