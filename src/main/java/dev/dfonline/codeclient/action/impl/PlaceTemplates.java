package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.MoveToLocation;
import dev.dfonline.codeclient.PlotLocation;
import dev.dfonline.codeclient.action.Action;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PlaceTemplates extends Action {
    public static final int rowSize = 6;

    private final List<ItemStack> templates;

    public int currentIndex = -1;
    private int timeSinceLastTick = 0;
    private ItemStack recoverMainHand;

    public PlaceTemplates(List<ItemStack> templates, Callback callback) {
        super(callback);
        this.templates = templates;
    }

    @Override
    public void init() {
        currentIndex = 0;
        recoverMainHand = CodeClient.MC.player.getMainHandItem();
    }

    static void makeHolding(ItemStack template) {
        Inventory inv = CodeClient.MC.player.getInventory();
        CodeClient.MC.getConnection().send(new ServerboundSetCreativeModeSlotPacket(36, template));
        inv.selected = 0;
        inv.setItem(0, template);
    }

    static void placeTemplateAt(int row, int level) {
        Vec3 pos = PlotLocation.getAsVec3d().add((2 + (row * 3)) * -1, level * 5, 0);
        new MoveToLocation(CodeClient.MC.player).setPos(pos.add(1,2,1));
        BlockHitResult blockHitResult = new BlockHitResult(pos.add(0,1,0), Direction.UP, new BlockPos.MutableBlockPos(pos.x, pos.y, pos.z), false);
        CodeClient.MC.gameMode.useItemOn(CodeClient.MC.player, InteractionHand.MAIN_HAND, blockHitResult);
    }

    @Override
    public void onTick() {
        if(currentIndex >= templates.size()) {
            currentIndex = -1;
            makeHolding(recoverMainHand);
            this.callback();
        };
        if(currentIndex == -1) return;
        timeSinceLastTick = timeSinceLastTick + 1;
        if(timeSinceLastTick == 3) {
            timeSinceLastTick = 0;
            makeHolding(templates.get(currentIndex));
            int level = currentIndex / rowSize;
            placeTemplateAt(currentIndex % rowSize, level);
            currentIndex += 1;
            if(currentIndex % rowSize == 0) {
                MoveToLocation.shove(CodeClient.MC.player, PlotLocation.getAsVec3d().add(0,(level * 5) + 2,0));
            }
        }
    }
}
