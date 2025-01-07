package dev.dfonline.codeclient.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.UpdateCheckerUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Mixin(UpdateCheckerUtil.class)
public class VersionChecker {
    @Unique
    private static final Logger LOGGER = UpdateCheckerUtil.LOGGER;

    @Inject(method = "checkForUpdates0", at = @At("TAIL"), remap = false)
    private static void checkVersion(CallbackInfo ci) {
        if (Config.getConfig().AutoUpdateOption == Config.AutoUpdate.OFF) return;
        Mod mod = ModMenu.MODS.get(CodeClient.MOD_ID);
        LOGGER.info("Checking for updates...");
        if (mod == null) {
            CodeClient.LOGGER.error("Failed to get mod info");
            return;
        }
        var updateInfo = mod.getUpdateInfo();
        if (updateInfo == null || !updateInfo.isUpdateAvailable()) {
            CodeClient.LOGGER.info("No updates available.");
            return;
        }
        if (updateInfo instanceof ModrinthUpdateInfo modrinthUpdateInfo) {
            CodeClient.LOGGER.warn("Update available! Branch: {}, Version: {}", modrinthUpdateInfo.getUpdateChannel(), modrinthUpdateInfo.getVersionId());

            // Get the new version.
            final String versionUrl = "https://api.modrinth.com/v2/version/" + modrinthUpdateInfo.getVersionId();

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(versionUrl))
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(CodeClient::parseVersionInfo)
                        .join();
            }
        }
    }
}
