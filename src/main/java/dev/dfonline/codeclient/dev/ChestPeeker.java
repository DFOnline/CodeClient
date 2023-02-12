package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.PlotLocation;
import dev.dfonline.codeclient.mixin.ClientWorldAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class ChestPeeker {
    private static BlockPos lastBlock = new BlockPos(0,0,0);
    private static int chestsToOpen = 0;

    public static void tick() {
        if(CodeClient.MC.hitResult instanceof BlockHitResult result) {
            BlockPos pos = result.getBlockPos();
            if(PlotLocation.isInCodeSpace(CodeClient.MC.player.position())) {
                if(!lastBlock.equals(pos)) {
                    OverlayManager.setOverlayText();
                    lookingLogic(result);
                } // if you aren't looking at your last block
            }
            lastBlock = pos;
        }
    }

    private static void lookingLogic(BlockHitResult result) {
        if(!(CodeClient.MC.level.getBlockEntity(result.getBlockPos()) instanceof ChestBlockEntity)) return;
        chestsToOpen += 1;
        CodeClient.MC.getConnection().send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, ((ClientWorldAccessor)CodeClient.MC.level).getBlockStatePredictionHandler().startPredicting().currentSequence()));
    }

    public static boolean onPacket(Packet<?> packet) {
        chestsToOpen = -1;
        if(chestsToOpen > 0) {
            if(packet instanceof ClientboundOpenScreenPacket screen) {
                CodeClient.MC.getConnection().send(new ServerboundContainerClosePacket(screen.getContainerId()));
                return true;
            }
            if(packet instanceof ClientboundContainerSetContentPacket inv) {
                chestsToOpen -= 1;
                OverlayManager.setOverlayText();
                OverlayManager.addOverlayText(Component.literal("Chest Contents:"));
                for (ItemStack item : inv.getItems().subList(0, 27)) {
                    if (item.getItem() == Items.AIR) continue;
                    OverlayManager.addOverlayText(item.getHoverName());
                }
                return true;
            }

            if(packet instanceof ClientboundSoundPacket) {
                return true;
            }
            if(packet instanceof ClientboundBlockEventPacket) {
                return true;
            }
        }
        return false;
    }
}
