package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.actiondump.Action;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class SlotGhostManager {
    private static Action action;
    private static AddWidget selectedSlot = null;

    public static void onClickChest(BlockHitResult hitResult) {
        action = null;
        selectedSlot = null;
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

    public static void drawSlot(DrawContext context, Slot slot) {
        try {
            if (!(CodeClient.MC.currentScreen instanceof GenericContainerScreen || CodeClient.MC.currentScreen instanceof CustomChestMenu)) {
                action = null;
                selectedSlot = null;
            }
            if (!goodAction() || slot.inventory instanceof PlayerInventory)
                return;
            if (slot.getIndex() >= action.icon.arguments.length) return;
            if (slot.hasStack()) return;
            var arg = action.icon.arguments[slot.getIndex()];
            Icon.Type type = Icon.Type.valueOf(arg.type);
            ItemStack itemStack = type.icon;
            if (itemStack.isEmpty()) return;
//        itemStack.setCount(slot.id);
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x30ff0000);
            context.drawItem(itemStack, slot.x, slot.y);
            context.drawItemInSlot(CodeClient.MC.textRenderer, itemStack, slot.x, slot.y);
//        context.drawText(CodeClient.MC.textRenderer, Text.literal(arg.type),slot.x,slot.y,0xFFFFFF00,true);
            context.fill(RenderLayer.getGuiGhostRecipeOverlay(), slot.x, slot.y, slot.x + 16, slot.y + 16, 822083583);
        } catch (Exception e) {
            context.drawText(CodeClient.MC.textRenderer, Text.literal("Error."), slot.x, slot.y, 0xFF0000, true);
        }
    }

    public static void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
        if (selectedSlot == null || !goodAction()) return;
        selectedSlot.render(context,mouseX,mouseY,handledScreen);
    }

    private static boolean goodAction() {
        return action != null && action.icon != null && action.icon.arguments != null;
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen, int x, int y) {
        if(selectedSlot != null) {
            selectedSlot.mouseClicked(mouseX,mouseY,button,screen,x,y);
            selectedSlot = null;
            return true;
        }
        return false;
    }

    private static class AddWidget {
        private int x;
        private int y;
        private List<ItemStack> options;
        private Slot slot;

        public AddWidget(Slot slot) {
            this.slot = slot;
            this.x = slot.x + 16;
            this.y = slot.y - 5;
            var arg = action.icon.arguments[slot.getIndex()];
            var type = arg.getType();
            var opt = new ArrayList<ItemStack>();
            if(type != Icon.Type.VARIABLE && type != null) {
                opt.add(type.icon);
            }
            opt.add(Icon.Type.VARIABLE.icon);
            options = opt;
        }

        public void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 900.0F);
            context.drawGuiTexture(new Identifier("recipe_book/overlay_recipe"), x, y, options.size() * 18 + 8, 26);
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
            int i = 0;
            for(var option: options) {
                context.drawItem(option, (x + 5) + (i * 18), y + 5);
                i++;
            }
            context.getMatrices().pop();
        }

        public void mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen, int x, int y) {
            if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + options.size() * 18 + 8 && mouseY < this.y + y + 26) {
                for (var option: options) {
                    if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + options.size() * 18 + 8 && mouseY < this.y + y + 26) {

                    }
                }
                Utility.debug("inside!");
            }
        }
    }

    public static void clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
        if (!slot.hasStack() && CodeClient.MC.player.currentScreenHandler.getCursorStack().isEmpty() && goodAction() && slot.getIndex() < action.icon.arguments.length)
            selectedSlot = new AddWidget(slot);
        else selectedSlot = null;
    }
}
