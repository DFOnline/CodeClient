package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ScanPlot extends Action {
    private static final Vec3d goToOffset = new Vec3d(0, 1.5, 0);
    private final ArrayList<ItemStack> returnList;
    private List<BlockPos> blocks = null;
    private HashMap<BlockPos, ItemStack> scanned = null;
    private Action step = null;

    public ScanPlot(Callback callback, ArrayList<ItemStack> scanList) {
        super(callback);
        this.returnList = scanList;
        if (!(CodeClient.location instanceof Dev)) {
            throw new IllegalStateException("Player must be in dev mode.");
        }
    }

    @Override
    public void init() {
        if (CodeClient.location instanceof Dev plot) {
            blocks = plot.scanForSigns(Pattern.compile("(PLAYER|ENTITY) EVENT|FUNCTION|PROCESS"), Pattern.compile(".*")).keySet().stream().toList();
            scanned = new HashMap<>(blocks.size());
        }
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if (step != null) return step.onReceivePacket(packet);
        return false;
//        var net = CodeClient.MC.getNetworkHandler();
//        if(waitForResponse && net != null && packet instanceof ScreenHandlerSlotUpdateS2CPacket slot) {
//            var data = Utility.templateDataItem(slot.getStack());
//            var template = Template.parse64(data);
//            if(template == null) return false;
//            scanList.add(Template.parse64(data));
//            waitForResponse = false;
//            progress += 1;
//            net.sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
//            return true;
//        }
    }

    @Nullable
    private BlockPos findNextBlock() {
        var player = CodeClient.MC.player.getPos();
        BlockPos nearest = null;
        for (BlockPos pos : blocks) {
            if (nearest == null || pos.isWithinDistance(player, nearest.toCenterPos().distanceTo(player))) {
                if (scanned.containsKey(pos) && scanned.get(pos) != null) {
                    continue;
                }
                nearest = pos;
            }
        }
        return nearest;
    }

    private void next() {
        var block = findNextBlock();
        if (block == null) {
            returnList.clear();
            returnList.addAll(scanned.values());
            callback();
            return;
        }
        step = new GoTo(block.toCenterPos().add(goToOffset), () -> {
            this.step = new PickUpBlock(block, () -> {
                this.step = null;
//                next(progress + 1);
            });
            this.step.init();
        });
        step.init();
    }

    @Override
    public void tick() {
        if (blocks == null) return;
        if (step != null) step.tick();
        else {
            next();
        }
    }


    private class PickUpBlock extends Action {
        private final BlockPos pos;
        private Integer ticks = 0;

        public PickUpBlock(BlockPos pos, Callback callback) {
            super(callback);
            this.pos = pos;
        }

        @Override
        public void init() {
            var net = CodeClient.MC.getNetworkHandler();
            Utility.makeHolding(ItemStack.EMPTY);
            var player = CodeClient.MC.player;
            var inter = CodeClient.MC.interactionManager;
            boolean sneaky = !player.isSneaking();
            if (sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            inter.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(this.pos.toCenterPos(), Direction.UP, this.pos, false));
            if (sneaky)
                net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        @Override
        public boolean onReceivePacket(Packet<?> packet) {
            var net = CodeClient.MC.getNetworkHandler();
            if (net != null && packet instanceof ScreenHandlerSlotUpdateS2CPacket slot) {
                var data = Utility.templateDataItem(slot.getStack());
                var template = Template.parse64(data);
                if (template == null) return false;
                scanned.put(pos, slot.getStack());
                net.sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
                this.callback();
                return true;
            }
            return super.onReceivePacket(packet);
        }

        @Override
        public void tick() {
            ticks += 1;
            if (ticks == 10) {
                ticks = 0;
                init();
            }
        }

        //        if(progress == blocks.size()) {
//            if(!waitForResponse) {
//                callback();
//            }
//            return;
//        }
//        var net = CodeClient.MC.getNetworkHandler();
//        var player = CodeClient.MC.player;
//        var inter = CodeClient.MC.interactionManager;
//        if(CodeClient.location instanceof Dev && this.blocks != null && net != null && player != null && inter != null) {
//            if(goTo == null || goTo.complete) {
//                if(goTo != null && !waitForResponse) {
//                    var current = blocks.get(progress);
//                    boolean sneaky = !player.isSneaking();
//                    if(sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//                    inter.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(current.toCenterPos(), Direction.UP, current,false));
//                    if(sneaky) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//                    waitForResponse = true;
//                    goTo = null;
//                }
//                goTo = new GoTo(blocks.get(progress).toCenterPos().add(goToOffset), () -> {});
//                goTo.init();
//            }
//            goTo.tick();
//        }
//    }
    }
}
