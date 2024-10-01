package dev.dfonline.codeclient;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Text found in the top left of the screen.
 */
public class OverlayManager {
    private static ArrayList<Text> overlayText = new ArrayList<>();

    private static Text cpuUsage = null;

    public static List<Text> getOverlayText() {
        return overlayText;
    }

    /**
     * Clears and sets the overlay text.
     */
    public static void setOverlayText(Text overlayText) {
        setOverlayText(new ArrayList<>(Collections.singleton(overlayText)));
    }

    /**
     * Clears and sets the overlay text.
     */
    public static void setOverlayText(ArrayList<Text> overlayText) {
        OverlayManager.overlayText = overlayText;
    }

    public static Text getCpuUsage() {
        return cpuUsage;
    }

    public static void setCpuUsage(Text cpuUsage) {
        OverlayManager.cpuUsage = cpuUsage;
    }

    /**
     * Clears the overlay text.
     */
    public static void setOverlayText() {
        setOverlayText(new ArrayList<>());
    }

    public static void addOverlayText(Text overlayText) {
        OverlayManager.overlayText.add(overlayText);
    }
}
