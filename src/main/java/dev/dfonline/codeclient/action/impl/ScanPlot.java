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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ScanPlot extends Action {
    private List<BlockPos> blocks = null;
    private Action step = null;
    private static final Vec3d goToOffset = new Vec3d(0, 1.5, 0);
    private final ArrayList<ItemStack> scanList;

    public ScanPlot(Callback callback, ArrayList<ItemStack> scanList) {
        super(callback);
        this.scanList = scanList;
        if (!(CodeClient.location instanceof Dev)) {
            throw new IllegalStateException("Player must be in dev mode.");
        }
    }

    @Override
    public void init() {
        if (CodeClient.location instanceof Dev plot) {
            blocks = plot.scanForSigns(Pattern.compile("(PLAYER|ENTITY) EVENT|FUNCTION|PROCESS"), Pattern.compile(".*")).keySet().stream().toList();
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

    private void next(int progress) {
        if(progress >= blocks.size()) {
            callback();
            return;
        }
        step = new GoTo(blocks.get(progress).toCenterPos().add(goToOffset), () -> {
            this.step = new pickUpBlock(blocks.get(progress),() -> {
                next(progress + 1);
            });
            this.step.init();
        });
        step.init();
    }

    @Override
    public void onTick() {
        if(blocks == null) return;
        if (step != null) step.onTick();
        else {
            next(0);
        }
    }


    private class pickUpBlock extends Action {
        private BlockPos pos;

        public pickUpBlock(BlockPos pos, Callback callback) {
            super(callback);
            this.pos = pos;
        }

        @Override
        public void init() {
            var net = CodeClient.MC.getNetworkHandler();
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
                scanList.add(slot.getStack());
                net.sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
                this.callback();
                return true;
            }
            return super.onReceivePacket(packet);
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
//            goTo.onTick();
//        }
//    }
    }
}
