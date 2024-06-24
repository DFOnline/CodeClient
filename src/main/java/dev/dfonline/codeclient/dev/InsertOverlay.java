package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestField;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.hypercube.item.Number;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class InsertOverlay {
    private static AddWidget selectedSlot = null;
    public static boolean isCodeChest = false;
    private static int screenX = 0;
    private static int screenY = 0;

    public static void reset() {
        selectedSlot = null;
        isCodeChest = false;
        screenX = 0;
        screenY = 0;
    }

    private static boolean overlayOpen() {
        return Config.getConfig().InsertOverlay && isCodeChest && selectedSlot != null;
    }

    public static void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen, int x, int y) {
        screenX = x;
        screenY = y;
        if (overlayOpen())
            selectedSlot.render(context, mouseX, mouseY, handledScreen);
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen) {
        if (overlayOpen()) {
            boolean stayOpen = selectedSlot.mouseClicked(mouseX, mouseY, button, screen);
            if (!stayOpen) {
                selectedSlot.close();
            }
            return true;
        }
        return false;
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (overlayOpen()) return selectedSlot.keyPressed(keyCode, scanCode, modifiers);
        return false;
    }

    public static boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (overlayOpen()) return selectedSlot.keyReleased(keyCode, scanCode, modifiers);
        return false;
    }

    public static boolean charTyped(char chr, int modifiers) {
        if (overlayOpen()) return selectedSlot.charTyped(chr, modifiers);
        return false;
    }

    public static boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(overlayOpen()) return selectedSlot.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return false;
    }

    public static void clickSlot(Slot slot, SlotActionType actionType) {
        if (Config.getConfig().InsertOverlay && isCodeChest && actionType == SlotActionType.PICKUP && !slot.hasStack() && CodeClient.MC.player.currentScreenHandler.getCursorStack().isEmpty())
            selectedSlot = new AddWidget(slot, () -> selectedSlot = null);
        else if (selectedSlot != null) selectedSlot.close();
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
            var opt = new ArrayList<Option>();

            int i = 0;
            for (var type : new VarItem[]{
                    new dev.dfonline.codeclient.hypercube.item.Text(),
                    new Component(),
                    new Number(),
                    new Location(),
                    new Vector(),
                    new Sound(),
//                    new Particle(),
                    new Potion(),
            }) {
                opt.add(new Option(type, x + 5 + i++ * 18, y + 5));
            }
            width = i * 18 + 8;
            height = 2 * 18 + 8;
            i = 0;
            for (var type : new VarItem[]{
                    new Variable(),
//                    new GameValue(),
//                    new Parameter(),
            }) {
                opt.add(new Option(type, x + 5 + i++ * 18, y + 18 + 5));
            }

            options = opt;
        }

        private void setSlot(ItemStack stack) {
            var mc = CodeClient.MC;
            var currentScreen = mc.currentScreen;
            var manager = mc.interactionManager;
            if (manager == null || mc.getNetworkHandler() == null) return;
            if (currentScreen instanceof HandledScreen<?> screen) {
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
            if (field != null && varItem != null) {
                setSlot(varItem.toStack());
            }
            this.close.run();
        }

        public void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 900.0F);
            context.drawGuiTexture(new Identifier("recipe_book/overlay_recipe"), x, y, width, height);
            if (field != null) {
                field.render(context, mouseX, mouseY, 0);
            } else {
                for (var option : options) {
                    context.drawItem(option.type.getIcon(), option.x, option.y);
                }
            }
            context.getMatrices().pop();
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button, HandledScreen<?> screen) {
            int x = screenX;
            int y = screenY;
            if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + width && mouseY < this.y + y + height) {
                if (field != null) {
                    boolean b = field.mouseClicked(mouseX - x, mouseY - y, button);
                    setSlot(varItem.toStack());
                    return b;
                }
                for (var option : options) {
                    if (mouseX > option.x + x && mouseY > option.y + y && mouseX < option.x + x + 16 && mouseY < option.y + y + 21) {
                        varItem = option.type;
                        var stack = varItem.toStack();
                        setSlot(stack);
                        if (screen instanceof CustomChestMenu customChestMenu) {
                            close();
                            customChestMenu.update();
                            return true;
                        }
                        this.width = 150;
                        this.height = 26;
                        field = new CustomChestField<>(CodeClient.MC.textRenderer, this.x + 5, this.y + 5, this.width - 10, this.height - 10, Text.literal(""), varItem);
                        field.select(true);
                        field.selectAll();
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (field != null) {
                if(keyCode != GLFW.GLFW_KEY_ENTER) {
                    var end = field.keyPressed(keyCode, scanCode, modifiers);
                    if(!end && keyCode == GLFW.GLFW_KEY_TAB) {
                        field.select((modifiers & GLFW.GLFW_MOD_SHIFT) != 1);
                    }
                }
                return true;
            }
            return false;
        }

        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            if (field != null) field.keyReleased(keyCode, scanCode, modifiers);
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                close();
                return true;
            }
            return false;
        }

        public boolean charTyped(char chr, int modifiers) {
            if (field != null) return field.charTyped(chr, modifiers);
            return false;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (field != null) return field.mouseScrolled(mouseX - screenX, mouseY - screenY, horizontalAmount, verticalAmount);
            return false;
        }


        private record Option(VarItem type, int x, int y) {
        }
    }
}
