package dev.dfonline.codeclient.dev.overlay;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SignPeeker {
    public static List<Text> getOverlayText() {
        if (!Config.getConfig().SignPeeker) return null;
        if(CodeClient.location instanceof Dev dev) {
            if (CodeClient.MC.crosshairTarget instanceof BlockHitResult block) {
                BlockPos pos = InteractionManager.isBlockBreakable(block.getBlockPos());
                if (pos == null || (!dev.isInDev(pos)) || CodeClient.MC.world == null) return null;
                if (CodeClient.MC.world.getBlockEntity(pos.west()) instanceof SignBlockEntity sign) {
                    SignText text = sign.getFrontText();
                    if (CodeClient.MC.player.getX() > pos.getX()) {
                        return List.of(
                                Text.empty().formatted(Formatting.GOLD).append(text.getMessage(0, false)),
                                text.getMessage(1, false),
                                Text.empty().formatted(Formatting.AQUA).append(text.getMessage(2, false)),
                                Text.empty().formatted(Formatting.RED).append(text.getMessage(3, false)));
                    }
                    Text name = text.getMessage(1, false);
                    if (CodeClient.MC.textRenderer.getWidth(name) > 90) return List.of(name);
                }
            }
        }
        return null;
    }
}
