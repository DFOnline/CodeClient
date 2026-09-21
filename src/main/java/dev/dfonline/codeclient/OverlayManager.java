package dev.dfonline.codeclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Text found in the top left of the screen.
 */
public class OverlayManager {
    private static ArrayList<Component> overlayText = new ArrayList<>();

    private static Component cpuUsage = null;

    public static List<Component> getOverlayText() {
        return overlayText;
    }

    /**
     * Clears and sets the overlay text.
     */
    public static void setOverlayText(Component overlayText) {
        setOverlayText(new ArrayList<>(Collections.singleton(overlayText)));
    }

    /**
     * Clears and sets the overlay text.
     */
    public static void setOverlayText(ArrayList<Component> overlayText) {
        OverlayManager.overlayText = overlayText;
    }

    public static Component getCpuUsage() {
        return cpuUsage;
    }

    public static void setCpuUsage(Component cpuUsage) {
        OverlayManager.cpuUsage = cpuUsage;
    }

    /**
     * Clears the overlay text.
     */
    public static void setOverlayText() {
        setOverlayText(new ArrayList<>());
    }

    public static void addOverlayText(Component overlayText) {
        OverlayManager.overlayText.add(overlayText);
    }
}
