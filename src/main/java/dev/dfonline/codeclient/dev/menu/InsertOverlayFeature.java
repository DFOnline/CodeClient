package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.ItemSelector;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestField;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.item.Number;
import dev.dfonline.codeclient.hypercube.item.*;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class InsertOverlayFeature extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().InsertOverlay;
    }

    @Override
    public ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new InsertOverlay(screen);
    }

    private static class InsertOverlay extends ChestFeature {
        private static final ItemStack searchIcon;
        private AddWidget selectedSlot = null;
        private int screenX = 0;
        private int screenY = 0;

        static {
            var item = Items.ITEM_FRAME.getDefaultStack();
            DFItem dfItem = DFItem.of(item);
            dfItem.setName(Text.translatable("itemGroup.search"));
            searchIcon = dfItem.getItemStack();
        }

        public InsertOverlay(HandledScreen<?> screen) {
            super(screen);
        }

        private boolean overlayOpen() {
            return selectedSlot != null;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, int x, int y, float delta) {
            screenX = x;
            screenY = y;
            if (overlayOpen())
                selectedSlot.render(context, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(Click click) {
            if (overlayOpen()) {
                boolean stayOpen = selectedSlot.mouseClicked(click, screen);
                if (!stayOpen) {
                    selectedSlot.close();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(KeyInput key) {
            if (overlayOpen()) return selectedSlot.keyPressed(key);
            return false;
        }

        @Override
        public boolean keyReleased(KeyInput key) {
            if (overlayOpen()) return selectedSlot.keyReleased(key);
            return false;
        }


        public boolean charTyped(CharInput input) {
            if (overlayOpen()) return selectedSlot.charTyped(input);
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (overlayOpen()) return selectedSlot.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return false;
        }

        @Override
        public boolean clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
            if(CodeClient.MC.player != null && slot.inventory == CodeClient.MC.player.getInventory()) return false;
            if (actionType == SlotActionType.PICKUP && !slot.hasStack() && CodeClient.MC.player.currentScreenHandler.getCursorStack().isEmpty())
                selectedSlot = new AddWidget(slot, () -> selectedSlot = null);
            else if (selectedSlot != null) selectedSlot.close();
            return false;
        }

        private class AddWidget {
            private final int x;
            private final int y;
            private int width;
            private int height;
            private final List<Option> options;
            private final Slot slot;
            private ClickableWidget field;
            private ItemStack item = null;
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
                    opt.add(new Option(type.toStack(), x + 5 + i++ * 18, y + 5));
                }
                width = i * 18 + 8;
                height = 2 * 18 + 8;
                i = 0;
                for (var type : new VarItem[]{
                        new Variable(),
//                    new GameValue(),
//                    new Parameter(),
                }) {
                    opt.add(new Option(type.toStack(), x + 5 + i++ * 18, y + 18 + 5));
                }

                opt.add(new Option(searchIcon, x + 5 + i * 18, y + 18 + 5));

                options = opt;
            }

            private void setSlot(@NotNull ItemStack stack) {
                if(stack == searchIcon) return;
                var mc = CodeClient.MC;
                var manager = mc.interactionManager;
                if (manager == null || mc.getNetworkHandler() == null) return;
                var currentScreen = mc.currentScreen;
                if (currentScreen instanceof HandledScreen<?> handledScreen) {
                    var player = mc.player;
                    var sync = handledScreen.getScreenHandler().syncId;
                    manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP, player);
                    mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, stack));
                    manager.clickSlot(sync, slot.id, 0, SlotActionType.SWAP, player);
                    manager.clickSlot(sync, 54, 0, SlotActionType.QUICK_CRAFT, player);
                    slot.setStack(stack);
                }
            }

            public void close() {
                if (field != null && item != null) {
                    if(field instanceof CustomChestField<?> chestField) item = chestField.item.toStack();
                }
                if(item != null) setSlot(item);
                this.close.run();
            }

            public void render(DrawContext context, int mouseX, int mouseY) {
//                context.getMatrices().translate(0.0F, 0.0F, 900.0F);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, Identifier.ofVanilla("recipe_book/overlay_recipe"), x, y, width, height);
                if (field != null) {
                    field.render(context, mouseX, mouseY, 0);
                } else {
                    for (var option : options) {
                        context.drawItem(option.type, option.x, option.y);
                    }
                }
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
            }

            public boolean mouseClicked(Click click, HandledScreen<?> screen) {
                var mouseX = click.x();
                var mouseY = click.y();
                int x = screenX;
                int y = screenY;
                if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + width && mouseY < this.y + y + height) {
                    if (field != null) {
                        boolean b = field.mouseClicked(new Click(mouseX - x, mouseY - y, click.buttonInfo()), false);
                        if(field instanceof CustomChestField<?> chestField) {
                            item = chestField.item.toStack();
                            setSlot(item);
                        }
                        return b;
                    }
                    for (var option : options) {
                        if (mouseX > option.x + x && mouseY > option.y + y && mouseX < option.x + x + 16 && mouseY < option.y + y + 21) {
                            item = option.type;
                            setSlot(item);
                            if(item != searchIcon) {
                                if (screen instanceof CustomChestMenu customChestMenu) {
                                    close();
                                    customChestMenu.update();
                                    return true;
                                }
                            }
                            this.width = 150;
                            VarItem varItem = VarItems.parse(item);
                            if(varItem != null) {
                                this.height = 26;
                                field = new CustomChestField<>(CodeClient.MC.textRenderer, this.x + 5, this.y + 5, this.width - 10, this.height - 10, Text.literal(""), varItem);
                                ((CustomChestField<?>) field).select(true);
                                ((CustomChestField<?>) field).selectAll();
                            }
                            else {
                                this.width = 160 + 10;
                                this.height = 134;
                                field = new ItemSelector(CodeClient.MC.textRenderer,this.x + 5, this.y + 5, this.width - 10, this.height - 10,screenX,screenY, itemStack -> {
                                    item = itemStack;
                                    close();
                                });
                            }
                            return true;
                        }
                    }
                }
                return false;
            }

            public boolean keyPressed(KeyInput key) {
                var keyCode = key.getKeycode();
                if (field != null) {
                    if (keyCode != GLFW.GLFW_KEY_ENTER) {
                        var end = field.keyPressed(key);
                        if (!end && keyCode == GLFW.GLFW_KEY_TAB && field instanceof CustomChestField<?> chestField) {
                            chestField.select((key.modifiers() & GLFW.GLFW_MOD_SHIFT) != 1);
                        }
                    }
                    return true;
                }
                return false;
            }

            public boolean keyReleased(KeyInput key) {
                var keyCode = key.getKeycode();
                if (field != null) field.keyReleased(key);
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    close();
                    return true;
                }
                return false;
            }

            public boolean charTyped(CharInput charInput) {
                if (field != null) return field.charTyped(charInput);
                return false;
            }

            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (field != null)
                    return field.mouseScrolled(mouseX - screenX, mouseY - screenY, horizontalAmount, verticalAmount);
                return false;
            }


            private record Option(ItemStack type, int x, int y) {
            }
        }
    }
}
