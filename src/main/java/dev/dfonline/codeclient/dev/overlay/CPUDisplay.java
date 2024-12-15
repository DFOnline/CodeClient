package dev.dfonline.codeclient.dev.overlay;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Plot;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;

import java.util.regex.Pattern;

public class CPUDisplay extends Feature {
    public int overlayTimeout = 0;

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (!(CodeClient.location instanceof Plot) || !Config.getConfig().CPUDisplay) {
            OverlayManager.setCpuUsage(null);
            return false;
        }
        if (!(packet instanceof OverlayMessageS2CPacket overlay)) return false;

        String txt = overlay.text().getString();

        Pattern pattern = Pattern.compile("CPU Usage: \\[" + "▮".repeat(20) + "] \\(\\d+\\.\\d+%\\)");
        if (!pattern.matcher(txt).matches()) return false;

        overlayTimeout = 2 * 20;
        OverlayManager.setCpuUsage(overlay.text());

        return true;
    }

    @Override
    public void tick() {
        if (overlayTimeout > 0) {
            overlayTimeout--;
        } else {
            OverlayManager.setCpuUsage(null);
        }
    }
}
