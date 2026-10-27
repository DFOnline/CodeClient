package dev.dfonline.codeclient.dev.overlay;

import net.minecraft.world.level.block.entity.SignTextSlot;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.location.Dev;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.BlockHitResult;

public class SignPeeker {
    public static List<Component> getOverlayText() {
        if (!Config.getConfig().SignPeeker) return null;
        if (CodeClient.location instanceof Dev dev) {
            if (CodeClient.MC.hitResult instanceof BlockHitResult block) {
                BlockPos pos = InteractionManager.isBlockBreakable(block.getBlockPos());
                if (pos == null || (!dev.isInDev(pos)) || CodeClient.MC.level == null) return null;
                if (CodeClient.MC.level.getBlockEntity(pos.west()) instanceof SignBlockEntity sign) {
                    SignText text = sign.getText(SignTextSlot.FRONT);
                    if (CodeClient.MC.player.getX() > pos.getX()) {
                        return List.of(
                                Component.empty().withStyle(ChatFormatting.GOLD).append(text.getMessages(false).get(0)),
                                text.getMessages(false).get(1),
                                Component.empty().withStyle(ChatFormatting.AQUA).append(text.getMessages(false).get(2)),
                                Component.empty().withStyle(ChatFormatting.RED).append(text.getMessages(false).get(3)));
                    }
                    Component name = text.getMessages(false).get(1);
                    if (CodeClient.MC.font.width(name) > 90) return List.of(name);
                }
            }
        }
        return null;
    }
}
