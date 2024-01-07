package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.Callback;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.mixin.entity.player.ClientPlayerInteractionManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceTemplates extends Action {
    public static final int rowSize = 6;
    private final ArrayList<Operation> operations;
    private int cooldown = 0;
    private ItemStack recoverMainHand;
    private GoTo goTo = null;
    private boolean shouldBeSwapping = false;

    public PlaceTemplates(List<ItemStack> templates, Callback callback) {
        super(callback);
        if(CodeClient.location instanceof Dev plot) {
            int i = 0;
            ArrayList<Operation> templatesToPlace = new ArrayList<>();
            for (ItemStack template: templates) {
                int row = i % rowSize;
                int level = i / rowSize;
                i++;
                Vec3d pos = new Vec3d(plot.getX(), 50, plot.getZ()).add((2 + (row * 3)) * -1, level * 5, 0);
                templatesToPlace.add(new TemplateToPlace(pos, template));
            }
            this.operations = templatesToPlace;
        }
        else {
            throw new IllegalStateException("Player must be in dev mode.");
        }
    }
    public PlaceTemplates(HashMap<BlockPos,ItemStack> templates, Callback callback) {
        super(callback);
        ArrayList<Operation> templatesToPlace = new ArrayList<>();
        for (var template: templates.entrySet()) {
            templatesToPlace.add(new TemplateToPlace(template.getKey(), template.getValue()));
        }
        this.operations = templatesToPlace;
    }

    /**
     * If there is a template in the way, replace it.
     */
    public PlaceTemplates swap() {
        this.shouldBeSwapping = true;
        return this;
    }

    @Override
    public void init() {
        cooldown = 4;
        recoverMainHand = CodeClient.MC.player.getMainHandStack();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof OpenScreenS2CPacket) {
            return true;
        }
        if(packet instanceof ChunkDeltaUpdateS2CPacket updates) {
            for (Operation operation : operations) {
                var block = new Object() {
                    boolean isTemplate = false;
                    BlockState state = null;
                };
                updates.visitUpdates((blockPos, blockState) -> {
                    if (operation.pos.equals(blockPos)) {
                        block.isTemplate = true;
                        block.state = blockState;
                    }
                });
                if (operation instanceof TemplateToPlace) {
                    if (!block.isTemplate) continue;
                    operation.setComplete();
                }
            }
        }
        return super.onReceivePacket(packet);
    }

    @Override
    public void onTick() {
        if(CodeClient.MC.interactionManager == null) return;
        if(CodeClient.location instanceof Dev) {
            if(operations.isEmpty()) {
                callback();
                return;
            }
            if(goTo != null) {
                goTo.onTick();
            }
            if(cooldown > 0) cooldown--;
            Operation closestOperation = null;
            operations.removeIf(Operation::isComplete);
            for (Operation operation: operations.stream().toList()) {
                double distanceToCurrentOperation = (operation.pos().distanceTo(CodeClient.MC.player.getEyePos()));
                if (distanceToCurrentOperation <= 5.8 && operation.isOpen() && !operation.isComplete()) {
                    if (cooldown == 0) {
                        if (operation instanceof TemplateToPlace template) {
                            if (shouldBeSwapping) {
                                var player = CodeClient.MC.player;
                                var net = CodeClient.MC.getNetworkHandler();
                                boolean sneaky = !player.isSneaking();
                                if (sneaky)
                                    net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                                net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, template.pos, Direction.UP));
                                net.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, template.pos, Direction.UP));
                                if (sneaky)
                                    net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                            }
                            Utility.makeHolding(template.template);
                            BlockHitResult blockHitResult = new BlockHitResult(template.pos().add(0, 1, 0), Direction.UP, template.pos, false);
                            ((ClientPlayerInteractionManagerAccessor) (CodeClient.MC.interactionManager)).invokeSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, sequence));
                            template.setOpen(false);
                        }
                    }
                    return;
                }
                if((closestOperation == null) || (distanceToCurrentOperation < closestOperation.pos().distanceTo(CodeClient.MC.player.getEyePos()))) {
                    closestOperation = operation;
                }
            }
            if(closestOperation == null) {
                for (Operation operation: operations) {
                    operation.setOpen(!operation.isComplete());
                }
            }
            else {
                goTo = new GoTo(closestOperation.pos().add(-2, 0.5, 0), () -> this.goTo = null);
                goTo.init();
                cooldown = 2;
            }
            return;
        }
    }

    private static abstract class Operation {
        protected final BlockPos pos;
        /**
         * If this operation can be acted on, and needs to be completed.
         */
        private boolean open = true;
        /**
         * Whether this operation is complete and can be dismissed.
         */
        private boolean complete = false;

        public Operation(BlockPos pos) {
            this.pos = pos;
        }
        public Operation(Vec3d pos) {
            this.pos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        }

        public Vec3d pos() {
            return pos.toCenterPos();
        }

        public void setOpen(boolean open) {
            this.open = open;
        }
        public boolean isOpen() {
            return open;
        }

        /**
         * Mark for dismissing. Doesn't need to be used again.
         */
        public void setComplete() {
            this.open = false;
            this.complete = true;
        }
        public boolean isComplete() {
            return complete;
        }
    }
    private static class TemplateToPlace extends Operation {
        private final ItemStack template;

        public TemplateToPlace(Vec3d pos, ItemStack template) {
            super(pos);
            this.template = template;
        }

        public TemplateToPlace(BlockPos pos, ItemStack template) {
            super(pos);
            this.template = template;
        }
    }
}
