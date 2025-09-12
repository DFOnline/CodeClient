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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Mixin(MultiplayerScreen.class)
public abstract class MMultiplayerScreen {
    @Unique
    private static final String[] UNOFFICIAL_DF_ADDRESSES = {"mcdiamondfire.net", "luke.cash", "reason.codes", "boobs.im", "mcdiamondfire.games"};
    @Unique
    private static final Pattern UNOFFICIAL_DF_ADDRESS_PATTERN = Pattern.compile("(?<prefix>\\w+\\.)?(?<address>" + String.join("|", UNOFFICIAL_DF_ADDRESSES) + ")(?<suffix>:\\d+)?");
    @Unique
    private static final String[] PLOT_SUBDOMAIN_ADDRESSES = {"mcdiamondfire.games"};
    @Unique
    private static final List<String> OFFICIAL_SUBDOMAINS = List.of("node1", "node2", "node3", "node4", "node5", "node6", "node7", "beta", "dev", "dev2", "dev3", "events");


    @Shadow
    private ServerList serverList;

    @Inject(method = "connect(Lnet/minecraft/client/network/ServerInfo;)V", at = @At("HEAD"))
    public void connect(ServerInfo entry, CallbackInfo ci) {
        var matcher = UNOFFICIAL_DF_ADDRESS_PATTERN.matcher(entry.address);

        if (!matcher.matches()) return;

        CodeClient.MC.getToastManager().add(SystemToast.create(
                CodeClient.MC,
                SystemToast.Type.PACK_LOAD_FAILURE,
                Text.translatable("codeclient.toast.unofficial_address.title"),
                Text.translatable("codeclient.toast.unofficial_address")
        ));

        var prefix = matcher.group("prefix");
        boolean isSubdomainAddress = Arrays.asList(PLOT_SUBDOMAIN_ADDRESSES).contains(matcher.group("address"));
        if (prefix == null || (!isSubdomainAddress && !OFFICIAL_SUBDOMAINS.contains(prefix.substring(0, prefix.length() - 1)))) {
            prefix = "";
        }
        String address = HypercubeConstants.SERVER_ADDRESS;
        if (isSubdomainAddress) {
            address = HypercubeConstants.SUBDOMAIN_SERVER_ADDRESS;
        }
        var suffix = matcher.group("suffix");
        if (suffix == null) {
            suffix = "";
        }
        entry.address = prefix + address + suffix;

        serverList.saveFile();
    }
}
