package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.mixin.ClientWorldAccessor;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChestPeeker {
    private static BlockPos lastBlock = new BlockPos(0,0,0);
    private static int chestsToOpen = 0;

    public static void tick() {
        if(CodeClient.MC.crosshairTarget instanceof BlockHitResult result) {
            Dev plot = (Dev) CodeClient.location;
            Vec3d playerPos = CodeClient.MC.player.getPos();
            BlockPos pos = result.getBlockPos();
            if(plot.isInCodeSpace(playerPos.getX(), playerPos.getZ())) {
                if(!lastBlock.equals(pos)) {
                    OverlayManager.setOverlayText();
                    lookingLogic(result);
                } // if you aren't looking at your last block
            }
            lastBlock = pos;
        }
    }

    private static void lookingLogic(BlockHitResult result) {
        if(!(CodeClient.MC.world.getBlockEntity(result.getBlockPos()) instanceof ChestBlockEntity)) return;
        chestsToOpen += 1;
        CodeClient.MC.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, ((ClientWorldAccessor)CodeClient.MC.world).getPendingUpdateManager().incrementSequence().getSequence()));
    }

    public static boolean onPacket(Packet<?> packet) {
        chestsToOpen = -1;
        if(chestsToOpen > 0) {
            if(packet instanceof OpenScreenS2CPacket screen) {
                CodeClient.MC.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(screen.getSyncId()));
                return true;
            }
            if(packet instanceof InventoryS2CPacket inv) {
                chestsToOpen -= 1;
                OverlayManager.setOverlayText();
                OverlayManager.addOverlayText(Text.literal("Chest Contents:"));
                for (ItemStack item : inv.getContents().subList(0, 27)) {
                    if (item.getItem() == Items.AIR) continue;
                    OverlayManager.addOverlayText(item.getName());
                }
                return true;
            }

            if(packet instanceof PlaySoundS2CPacket) {
                return true;
            }
            if(packet instanceof BlockEventS2CPacket) {
                return true;
            }
        }
        return false;
    }
}
