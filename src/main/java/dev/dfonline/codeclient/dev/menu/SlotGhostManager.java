package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.InteractionManager;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.actiondump.Action;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Argument;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import dev.dfonline.codeclient.location.Dev;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public class SlotGhostManager extends Feature {
    private Action action;
    private float time = 0.0F;

    @Override
    public void reset() {
        action = null;
        time = 0;
    }

    @Override
    public boolean enabled() {
        return Config.getConfig().ParameterGhosts;
    }

    @Override
    public void tick() {
        if(CodeClient.MC.gui.screen() == null && !InteractionManager.isOpeningCodeChest) {
            reset();
        }
    }

    @Override
    public void onClickChest(BlockHitResult hitResult) {
        action = null;
        if (CodeClient.MC.level == null) return;
        var pos = hitResult.getBlockPos();
        if (CodeClient.location instanceof Dev dev && dev.isInDev(pos) && CodeClient.MC.level.getBlockEntity(pos) instanceof ChestBlockEntity) {
            var signEntity = CodeClient.MC.level.getBlockEntity(hitResult.getBlockPos().below().west());
            if (signEntity instanceof SignBlockEntity sign) {
                try {
                    action = ActionDump.getActionDump().findAction(sign.getFrontText());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public @Nullable ChestFeature makeChestFeature(AbstractContainerScreen<?> screen) {
        return new GhostSlots(screen);
    }

    class GhostSlots extends ChestFeature {
        public GhostSlots(AbstractContainerScreen<?> screen) {
            super(screen);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, int x, int y, float delta) {
            if (!CodeClient.MC.hasControlDown()) {
                time += delta;
            }
        }

        @Nullable
        public Argument getArgument(Slot slot) {
            List<Argument> args = new ArrayList<>();

            for (Icon.ArgumentGroup group : action.icon.getArgGroups()) {
                List<Icon.ArgumentGroup.ArgumentPossibilities> pos = group.getPossibilities();
                args.addAll(pos.get((int) (time / 30F) % pos.size()).arguments());
            }

            if (slot.getContainerSlot() >= args.size()) return null;

            return args.get(slot.getContainerSlot());
        }

        public void drawSlot(GuiGraphics context, Slot slot) {
            if (!(CodeClient.MC.gui.screen() instanceof ContainerScreen || CodeClient.MC.gui.screen() instanceof CustomChestMenu)) {
                action = null;
            }
            if (badAction() || slot.container instanceof Inventory)
                return;
            if (slot.hasItem()) return;
            Argument arg = getArgument(slot);
            if (arg == null) return;
            ItemStack itemStack = arg.getItem();

            if (itemStack.isEmpty()) return;
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, arg.optional ? 0xA0__90_90_FF : 0x60__FF_00_00);
            context.renderItem(itemStack, slot.x, slot.y);
            context.renderItemDecorations(CodeClient.MC.font, itemStack, slot.x, slot.y);
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x40_FFFFFF);
        }

        @Override @Nullable
        public ItemStack getHoverStack(Slot slot) {
            if (badAction() || slot.container instanceof Inventory)
                return null;
            if (slot.hasItem()) return null;
            Argument arg = getArgument(slot);
            if (arg == null) return null;
            return arg.getItem();
        }
    }

    private boolean badAction() {
        return action == null || action.icon == null || action.icon.arguments == null;
    }
}
