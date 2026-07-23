package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.hypercube.template.Template;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.location.Plot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ScanPlot extends Action {
    private static final Vec3 goToOffset = new Vec3(0, 1.5, 0);
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
            blocks = plot.scanForSigns(Plot.lineStarterPattern, Pattern.compile(".*")).keySet().stream().toList();
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
        var player = CodeClient.MC.player.position();
        BlockPos nearest = null;
        for (BlockPos pos : blocks) {
            if (nearest == null || pos.closerToCenterThan(player, Vec3.atCenterOf(nearest).distanceTo(player))) {
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
        step = new GoTo(Vec3.atCenterOf(block).add(goToOffset), () -> {
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
            var net = CodeClient.MC.getConnection();
            Utility.makeHolding(ItemStack.EMPTY);
            var player = CodeClient.MC.player;
            var inter = CodeClient.MC.gameMode;
            boolean sneaky = !player.isShiftKeyDown();
            if (sneaky) net.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
            inter.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(this.pos), Direction.UP, this.pos, false));
            if (sneaky) net.send(new ServerboundPlayerInputPacket(CodeClient.MC.player.getLastSentInput()));
        }

        @Override
        public boolean onReceivePacket(Packet<?> packet) {
            var net = CodeClient.MC.getConnection();
            if (net != null && packet instanceof ClientboundContainerSetSlotPacket slot) {
                var data = Utility.templateDataItem(slot.getItem());
                var template = Template.parse64(data);
                if (template == null) return false;
                scanned.put(pos, slot.getItem());
                net.send(new ServerboundSetCreativeModeSlotPacket(slot.getSlot(), ItemStack.EMPTY));
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
