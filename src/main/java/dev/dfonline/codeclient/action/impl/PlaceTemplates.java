package dev.dfonline.codeclient.action.impl;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.MoveToLocation;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.Action;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.mixin.entity.player.ClientPlayerInteractionManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceTemplates extends Action {
    public static final int rowSize = 6;
    private final ArrayList<TemplateToPlace> templates;
    private int cooldown = 0;
    private ItemStack recoverMainHand;
    private GoTo goTo = null;

    public PlaceTemplates(List<ItemStack> templates, Callback callback) {
        super(callback);
        if(CodeClient.location instanceof Dev plot) {
            int i = 0;
            ArrayList<TemplateToPlace> templatesToPlace = new ArrayList<>();
            for (ItemStack template: templates) {
                int row = i % rowSize;
                int level = i / rowSize;
                i++;
                Vec3d pos = new Vec3d(plot.getX(), 50, plot.getZ()).add((2 + (row * 3)) * -1, level * 5, 0);
                templatesToPlace.add(new TemplateToPlace(pos, template));
            }
            this.templates = templatesToPlace;
        }
        else {
            throw new IllegalStateException("Player must be in dev mode.");
        }
    }

    @Override
    public void init() {
        cooldown = 4;
        recoverMainHand = CodeClient.MC.player.getMainHandStack();
    }

    @Override
    public boolean onReceivePacket(Packet<?> packet) {
        if(packet instanceof OpenScreenS2CPacket) return true;
        if(packet instanceof ChunkDeltaUpdateS2CPacket updates) {
            for (TemplateToPlace template : templates) {
                var block = new Object() {
                    boolean isTemplate = false;
                    BlockState state = null;
                };
                updates.visitUpdates((blockPos, blockState) -> {
                    if (template.pos.equals(blockPos)) {
                        block.isTemplate = true;
                        block.state = blockState;
                    }
                });
                if (!block.isTemplate) continue;
                template.setComplete();
            }
        }
        return super.onReceivePacket(packet);
    }

    @Override
    public void onTick() {
        if(CodeClient.MC.interactionManager == null) return;
        if(CodeClient.location instanceof Dev) {
            if(templates.size() == 0) {
                callback();
                return;
            }
            if(goTo != null) {
                goTo.onTick();
            }
            if(cooldown > 0) cooldown--;
            TemplateToPlace template = templates.stream().filter(TemplateToPlace::isOpen).findFirst().orElse(null);
            if(template == null) {
                templates.removeIf(TemplateToPlace::isComplete);
                for(TemplateToPlace templateToPlace : templates) templateToPlace.setOpen(!templateToPlace.isComplete());
                return;
            }
            if(template.pos().distanceTo(CodeClient.MC.player.getEyePos()) > 5.8) {
                goTo = new GoTo(template.pos().add(-2, 0.5, 0), () -> this.goTo = null);
                goTo.init();
                cooldown = 2;
                return;
            }
            if(cooldown == 0) {
                Utility.makeHolding(template.template);
                BlockHitResult blockHitResult = new BlockHitResult(template.pos().add(0,1,0), Direction.UP, template.pos, false);
                ((ClientPlayerInteractionManagerAccessor) (CodeClient.MC.interactionManager)).invokeSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, sequence));
                template.setOpen(false);
            }
        }
    }

    private static class TemplateToPlace {
        private final BlockPos pos;
        private final ItemStack template;
        private boolean open = true;
        private boolean complete = false;


        public TemplateToPlace(Vec3d pos, ItemStack template) {
            this.pos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
            this.template = template;
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
        public void setComplete() {
            this.open = false;
            this.complete = true;
        }

        public boolean isComplete() {
            return complete;
        }
    }
}
