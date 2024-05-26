package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestField;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.actiondump.Action;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import dev.dfonline.codeclient.hypercube.item.VarItem;
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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SlotGhostManager {
    private static Action action;
    private static AddWidget selectedSlot = null;

    public static void reset() {
        selectedSlot = null;
        action = null;
    }

    public static void tick() {
        if(CodeClient.MC.currentScreen == null) {
//            reset();
        }
    }

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
            boolean stayOpen = selectedSlot.mouseClicked(mouseX,mouseY,button,screen,x,y);
            if(!stayOpen) {
                selectedSlot = null;
            }
            return true;
        }
        return false;
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(selectedSlot != null) {
            return selectedSlot.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public static boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(selectedSlot != null) {
            return selectedSlot.keyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public static boolean charTyped(char chr, int modifiers) {
        if(selectedSlot != null) {
            return selectedSlot.charTyped(chr, modifiers);
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
        private CustomChestField<?> field;
        private VarItem varItem = null;
        private final Runnable close;

        public AddWidget(Slot slot, Runnable close) {
            this.slot = slot;
            this.close = close;
            this.x = slot.x + 16;
            this.y = slot.y - 5;
            var arg = action.icon.arguments[slot.getIndex()];
            var type = arg.getType();
            var opt = new ArrayList<Option>();
            int i = 0;
            if(type != Icon.Type.VARIABLE && type != null) {
                if(type.getVarItem != null) {
                    opt.add(new Option(type, x + 5, y + 5));
                    i++;
                }
            }
            opt.add(new Option(Icon.Type.VARIABLE, x + 5 + i * 18,y + 5));
            options = opt;
            width = options.size() * 18 + 8;
        }

        private void setSlot(ItemStack stack) {
            var mc = CodeClient.MC;
            var currentScreen = mc.currentScreen;
            var manager = mc.interactionManager;
            if (manager == null || mc.getNetworkHandler() == null) return;
            if(currentScreen instanceof HandledScreen<?> screen) {
                var player = mc.player;
                var sync = screen.getScreenHandler().syncId;
                manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP, player);
                mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, stack));
                manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP, player);
                manager.clickSlot(sync, 54, 0, SlotActionType.QUICK_CRAFT, player);
                slot.setStack(stack);
            }
        }

        public void close() {
            if(field != null && varItem != null) {
                setSlot(varItem.toStack());
            }
            this.close.run();
        }

        public void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 900.0F);
            context.drawGuiTexture(new Identifier("recipe_book/overlay_recipe"), x, y, width, height);
            if(field != null) {
                field.render(context,mouseX,mouseY,0);
            }
            else {
                for(var option: options) {
                    context.drawItem(option.type.getIcon(), option.x, option.y);
                }
            }
            context.getMatrices().pop();
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen, int x, int y) {
            if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + width && mouseY < this.y + y + height) {
                if(field != null) {
                    boolean b = field.mouseClicked(mouseX - x, mouseY - y, button);
                    setSlot(varItem.toStack());
                    return b;
                }
                for (var option : options) {
                    if (mouseX > option.x + x && mouseY > option.y + y && mouseX < option.x + x + 16 && mouseY < option.y + y + 21) {
                        if (option.type.getVarItem == null) return false;
                        varItem = option.type.getVarItem.run();
                        var stack = varItem.toStack();
                        setSlot(stack);
                        if(screen instanceof CustomChestMenu customChestMenu) {
                            close();
                            customChestMenu.update();
                            return true;
                        }
                        this.width = 100;
                        this.height = 26;
                        field = new CustomChestField<>(CodeClient.MC.textRenderer,this.x + 5,this.y + 5,this.width - 10,this.height - 10,Text.literal(""), varItem);
                        field.setFocused(true);
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if(field != null) {
                field.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }
            return false;
        }

        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            if(field != null) field.keyReleased(keyCode, scanCode, modifiers);
            if(keyCode == GLFW.GLFW_KEY_ENTER) {
                close();
                return true;
            }
            return false;
        }

        public boolean charTyped(char chr, int modifiers) {
            if(field != null) {
                return field.charTyped(chr,modifiers);
            }
            return false;
        }


        private record Option(Icon.Type type, int x, int y) {}
    }

    public static void clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
        if (actionType == SlotActionType.PICKUP && !slot.hasStack() && CodeClient.MC.player.currentScreenHandler.getCursorStack().isEmpty() && goodAction() && slot.getIndex() < action.icon.arguments.length)
            selectedSlot = new AddWidget(slot, () -> selectedSlot = null);
        else if(selectedSlot != null) selectedSlot.close();
    }
}
