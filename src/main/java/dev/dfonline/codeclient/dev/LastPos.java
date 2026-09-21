package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.action.impl.GoTo;
import dev.dfonline.codeclient.location.Creator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.sounds.SoundEvents;

public class LastPos {
    public static void handlePacket(Packet<?> packet) {
        if (CodeClient.MC.player == null) return;
        if (CodeClient.location instanceof Creator plot) {
            if (plot.getSize() != null && !plot.isInPlot(BlockPos.containing(CodeClient.MC.player.position())))
                return; // todo: make a player version for this
            if (!(packet instanceof ClientboundPlayerPositionPacket)) return;
            plot.devPos = CodeClient.MC.player.position();
        }
    }

    public static boolean tpBack() {
        if (CodeClient.MC.player == null) return false;
        if (CodeClient.location instanceof Creator plot && CodeClient.currentAction instanceof None) {
            if (plot.devPos == null) return false;
            CodeClient.currentAction = new GoTo(plot.devPos, () -> {
                CodeClient.MC.player.playSound(SoundEvents.ENDERMAN_TELEPORT, 2, 1);
                CodeClient.currentAction = new None();
            });
            CodeClient.currentAction.init();
            return true;
        }
        return false;
    }
}
