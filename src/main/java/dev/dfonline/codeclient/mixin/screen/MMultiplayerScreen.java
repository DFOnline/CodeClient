package dev.dfonline.codeclient.mixin.screen;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.HypercubeConstants;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Pattern;

@Mixin(MultiplayerScreen.class)
public abstract class MMultiplayerScreen {
    @Unique
    private static final String[] unofficialDFAddresses = {"mcdiamondfire.net", "luke.cash", "reason.codes", "boobs.im"};

    @Shadow
    private ServerList serverList;

    @Inject(method = "connect(Lnet/minecraft/client/network/ServerInfo;)V", at = @At("HEAD"))
    public void connect(ServerInfo entry, CallbackInfo ci) {
        var addressPattern = String.join("|", unofficialDFAddresses);
        var regex = Pattern.compile("(?<prefix>\\w+\\.)?(" + addressPattern + ")(?<suffix>:\\d+)?");
        var matcher = regex.matcher(entry.address);

        if (!matcher.matches()) return;

        CodeClient.MC.getToastManager().add(new SystemToast(
                SystemToast.Type.UNSECURE_SERVER_WARNING,
                Text.translatable("codeclient.toast.unofficial_address.title"),
                Text.translatable("codeclient.toast.unofficial_address")
        ));

        var prefix = matcher.group("prefix");
        // This check is needed for domains that use a subdomain for the diamondfire redirect, such as `df.domain.tld`,
        // which would turn the IP to `df.mcdiamondfire.com` instead of `mcdiamondfire.com`.
        if (prefix == null ||
                // 'dev3' does not exist, but is there for future proofing.
                !List.of("node1", "node2", "node3", "node4", "node5", "node6", "node7", "beta", "dev", "dev2", "dev3", "events").contains(prefix)
        ) prefix = "";
        var suffix = matcher.group("suffix");
        if (suffix == null) suffix = "";
        entry.address = prefix + HypercubeConstants.SERVER_ADDRESS + suffix;

        serverList.saveFile();
    }
}
