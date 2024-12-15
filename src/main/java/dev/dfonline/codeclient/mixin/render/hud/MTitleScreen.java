package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.toast.SystemToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MTitleScreen {
    @Inject(method = "init", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        if (CodeClient.startupToast != null) {
            // UNSECURE_SERVER_WARNING so the toast stays longer, there is no visual difference.
            CodeClient.MC.getToastManager().add(new SystemToast(SystemToast.Type.UNSECURE_SERVER_WARNING, CodeClient.startupToast.title(), CodeClient.startupToast.description()));
            CodeClient.startupToast = null;
        }

        if (CodeClient.autoJoin == CodeClient.AutoJoin.GAME) {
            ServerInfo info = new ServerInfo("DiamondFire", Config.getConfig().AutoNode.prepend + "mcdiamondfire.com", ServerInfo.ServerType.OTHER);
            ConnectScreen.connect((TitleScreen) (Object) this, CodeClient.MC, ServerAddress.parse(info.address), info, true, null);

            CodeClient.autoJoin = Config.getConfig().AutoJoinPlot ? CodeClient.AutoJoin.PLOT : CodeClient.AutoJoin.NONE;
        }
    }
}
