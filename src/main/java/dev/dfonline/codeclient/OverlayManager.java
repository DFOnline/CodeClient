package dev.dfonline.codeclient;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Text found in the top left of the screen.
 */
public class OverlayManager {
    private static ArrayList<Text> overlayText = new ArrayList<>();

    public static List<Text> getOverlayText() {
        return overlayText;
    }

    /**
     * Clears the overlay text.
     */
    public static void setOverlayText() {
        setOverlayText(new ArrayList<>());
    }

    /**
     * Clears and sets the overlay text.
     */
    public static void setOverlayText(ArrayList<Text> overlayText) {
        OverlayManager.overlayText = overlayText;
    }

    public static void addOverlayText(Text overlayText) {
        OverlayManager.overlayText.add(overlayText);
    }
}
