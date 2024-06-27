package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Play;
import dev.dfonline.codeclient.location.Spawn;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class BuildPhaser {
    private static boolean clipping = false;
    private static long stopClipTimestamp;
    private static GameMode preClipGameMode;

    public static boolean isClipping() {
        return clipping;
    }

    public static void tick() {

        if (cantClip()) return;

        if (!clipping && KeyBinds.clipBind.isPressed()) {
            clipping = true;
            if (CodeClient.MC.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
                preClipGameMode = CodeClient.MC.interactionManager.getCurrentGameMode();
                CodeClient.MC.getNetworkHandler().sendCommand("gmsp");
            }
        }

        if (clipping && !KeyBinds.clipBind.isPressed()) {
            stopClipping();
        }
    }

    public static boolean cantClip() {
        // If the player is at spawn & the network handler/interaction manager are null
        return  CodeClient.location instanceof Spawn
                ||CodeClient.MC.getNetworkHandler() == null
                || CodeClient.MC.interactionManager == null;
    }

    public static void stopClipping() {
        if (cantClip()) return;
        // If started clipping in spectator mode switch to creative mode
        if (preClipGameMode == null || preClipGameMode == GameMode.SPECTATOR) preClipGameMode = GameMode.CREATIVE;
        // If started clipping in survival/adventure mode, but we are in dev mode, switch to creative mode
        if (!(CodeClient.location instanceof Play) &&
                preClipGameMode == GameMode.SURVIVAL || preClipGameMode == GameMode.ADVENTURE) preClipGameMode = GameMode.CREATIVE;
        CodeClient.MC.getNetworkHandler().sendCommand("gamemode " + preClipGameMode.getName());
        clipping = false;
        stopClipTimestamp = System.currentTimeMillis();
    }

    public static boolean shouldCancelMessage(Text message) {
        // Cancel gamemode messages if currently clipping or stopped clipping in the last half a second
        return (clipping || System.currentTimeMillis() - stopClipTimestamp < 500) && message.getString().contains("Set your gamemode to");
    }
}
