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
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
        if(CodeClient.MC.currentScreen == null && !InteractionManager.isOpeningCodeChest) {
            reset();
        }
    }

    @Override
    public void onClickChest(BlockHitResult hitResult) {
        action = null;
        if (CodeClient.MC.world == null) return;
        var pos = hitResult.getBlockPos();
        if (CodeClient.location instanceof Dev dev && dev.isInDev(pos) && CodeClient.MC.world.getBlockEntity(pos) instanceof ChestBlockEntity) {
            var signEntity = CodeClient.MC.world.getBlockEntity(hitResult.getBlockPos().down().west());
            if (signEntity instanceof SignBlockEntity sign) {
                try {
                    action = ActionDump.getActionDump().findAction(sign.getFrontText());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public @Nullable ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new GhostSlots(screen);
    }

    class GhostSlots extends ChestFeature {
        public GhostSlots(HandledScreen<?> screen) {
            super(screen);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, int x, int y, float delta) {
            if (!Screen.hasControlDown()) {
                time += delta;
            }
        }

        @Nullable
        public Argument getArgument(Slot slot) {
            var args = new ArrayList<Argument>();

            for (var group : action.icon.getArgGroups()) {
                var pos = group.getPossibilities();
                args.addAll(pos.get((int) (time / 30F) % pos.size()).arguments());
            }

            if (slot.getIndex() >= args.size()) return null;

            return args.get(slot.getIndex());
        }

        public void drawSlot(DrawContext context, Slot slot) {
            try {
                if (!(CodeClient.MC.currentScreen instanceof GenericContainerScreen || CodeClient.MC.currentScreen instanceof CustomChestMenu)) {
                    action = null;
                }
                if (badAction() || slot.inventory instanceof PlayerInventory)
                    return;
                if (slot.hasStack()) return;
                var arg = getArgument(slot);
                if (arg == null) return;
                Icon.Type type = Icon.Type.valueOf(arg.type);
                ItemStack itemStack = type.getIcon();
                if (itemStack.isEmpty()) return;
//        itemStack.setCount(slot.id);
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, arg.optional ? 0x50__90_90_ff : 0x30__ff_00_00);
                context.drawItem(itemStack, slot.x, slot.y);
                context.drawStackOverlay(CodeClient.MC.textRenderer, itemStack, slot.x, slot.y);
//        context.drawText(CodeClient.MC.textRenderer, Text.literal(arg.type),slot.x,slot.y,0xFFFFFF00,true);
                context.fill(RenderLayer.getGuiGhostRecipeOverlay(), slot.x, slot.y, slot.x + 16, slot.y + 16, 0x40ffffff);
            } catch (Exception e) {
                context.drawText(CodeClient.MC.textRenderer, Text.literal("Error."), slot.x, slot.y, 0xFF0000, true);
            }
        }

        @Override @Nullable
        public ItemStack getHoverStack(Slot slot) {
            if (badAction() || slot.inventory instanceof PlayerInventory)
                return null;
            if (slot.hasStack()) return null;
            var arg = getArgument(slot);
            if (arg == null) return null;
            return arg.getItem();
        }
    }

    private boolean badAction() {
        return action == null || action.icon == null || action.icon.arguments == null;
    }
}
