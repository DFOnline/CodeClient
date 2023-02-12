package dev.dfonline.codeclient;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
    private static ArrayList<net.minecraft.network.chat.Component> overlayText = new ArrayList<>();

    public static List<net.minecraft.network.chat.Component> getOverlayText() {
        return overlayText;
    }

    public static void setOverlayText() {
        setOverlayText(new ArrayList<>());
    }
    
    public static void setOverlayText(ArrayList<net.minecraft.network.chat.Component> overlayText) {
        OverlayManager.overlayText = overlayText;
    }

    public static void addOverlayText(net.minecraft.network.chat.Component overlayText) {
        OverlayManager.overlayText.add(overlayText);
    }
}
