package dev.dfonline.codeclient.mixin.render.hud;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.gui.components.toasts.SystemToast;
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
    public void onInit(CallbackInfo ci) {
        if (CodeClient.startupToast != null) {
            CodeClient.MC.getToastManager().addToast(SystemToast.multiline(
                    CodeClient.MC,
                    SystemToast.SystemToastId.PACK_LOAD_FAILURE,
                    CodeClient.startupToast.title(),
                    CodeClient.startupToast.description())
            );
            CodeClient.startupToast = null;
        }

        if (CodeClient.autoJoin == CodeClient.AutoJoin.GAME) {
            ServerData info = new ServerData("DiamondFire", Config.getConfig().AutoNode.prepend + "mcdiamondfire.com", ServerData.Type.OTHER);
            ConnectScreen.startConnecting((TitleScreen) (Object) this, CodeClient.MC, ServerAddress.parseString(info.ip), info, true, null);

            CodeClient.autoJoin = Config.getConfig().AutoJoinPlot ? CodeClient.AutoJoin.PLOT : CodeClient.AutoJoin.NONE;
        }
    }
}
