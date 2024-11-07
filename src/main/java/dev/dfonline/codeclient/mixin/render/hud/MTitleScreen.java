package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MTitleScreen {
    @Inject(method = "init", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        if (CodeClient.autoJoin == CodeClient.AutoJoin.GAME) {
            ServerInfo info = new ServerInfo("DiamondFire", Config.getConfig().AutoNode.prepend + "mcdiamondfire.com", ServerInfo.ServerType.OTHER);
            ConnectScreen.connect((TitleScreen) (Object) this, CodeClient.MC, ServerAddress.parse(info.address), info, true, null);

            CodeClient.autoJoin = Config.getConfig().AutoJoinPlot ? CodeClient.AutoJoin.PLOT : CodeClient.AutoJoin.NONE;
        }
    }
}
