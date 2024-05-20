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
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
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
            ItemStack itemStack = type.getIcon();
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
        private int width;
        private int height = 26;
        private List<Option> options;
        private Slot slot;

        public AddWidget(Slot slot) {
            this.slot = slot;
            this.x = slot.x + 16;
            this.y = slot.y - 5;
            var arg = action.icon.arguments[slot.getIndex()];
            var type = arg.getType();
            var opt = new ArrayList<Option>();
            int i = 0;
            if(type != Icon.Type.VARIABLE && type != null) {
                opt.add(new Option(type, x + 5,y + 5));
                i++;
            }
            opt.add(new Option(Icon.Type.VARIABLE, x + 5 + i * 18,y + 5));
            options = opt;
            width = options.size() * 18 + 8;
        }

        public void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 900.0F);
            context.drawGuiTexture(new Identifier("recipe_book/overlay_recipe"), x, y, width, height);
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
            int i = 0;
            for(var option: options) {
                context.drawItem(option.type.getIcon(), option.x, option.y);
                i++;
            }
            context.getMatrices().pop();
        }

        public void mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen, int x, int y) {
            if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + width && mouseY < this.y + y + height) {
                for (var option: options) {
                    if (mouseX > option.x + x && mouseY > option.y + y && mouseX < option.x + x + 16 && mouseY < option.y + y + 21) {
                        var mc = CodeClient.MC;
                        var manager = mc.interactionManager;
                        Utility.debug("inside!");
                        if(manager == null) return;
                        var player = mc.player;
                        var sync = screen.getScreenHandler().syncId;
                        manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP,player);
                        mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, option.type.getIcon()));
                        manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP,player);
                        manager.clickSlot(sync, 54, 0, SlotActionType.QUICK_CRAFT,player);
                    }
                }
            }
        }

        private record Option(Icon.Type type, int x, int y) {}
    }

    public static void clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
        if (actionType == SlotActionType.PICKUP && !slot.hasStack() && CodeClient.MC.player.currentScreenHandler.getCursorStack().isEmpty() && goodAction() && slot.getIndex() < action.icon.arguments.length)
            selectedSlot = new AddWidget(slot);
        else selectedSlot = null;
    }
}
