package dev.dfonline.codeclient.dev.menu;

import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.ItemSelector;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestField;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.item.*;
import dev.dfonline.codeclient.hypercube.item.Number;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InsertOverlayFeature extends Feature {
    @Override
    public boolean enabled() {
        return Config.getConfig().InsertOverlay;
    }

    @Override
    public ChestFeature makeChestFeature(AbstractContainerScreen<?> screen) {
        return new InsertOverlay(screen);
    }

    private static class InsertOverlay extends ChestFeature {
        private static final ItemStack searchIcon;
        private AddWidget selectedSlot = null;
        private int screenX = 0;
        private int screenY = 0;

        static {
            var item = Items.ITEM_FRAME.getDefaultInstance();
            DFItem dfItem = DFItem.of(item);
            dfItem.setName(Component.translatable("itemGroup.search"));
            searchIcon = dfItem.getItemStack();
        }

        public InsertOverlay(AbstractContainerScreen<?> screen) {
            super(screen);
        }

        private boolean overlayOpen() {
            return selectedSlot != null;
        }


        @Override
        public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, float delta) {
            screenX = x;
            screenY = y;
            if (overlayOpen())
                selectedSlot.extractRenderState(graphics, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click) {
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
        public boolean keyPressed(KeyEvent key) {
            if (overlayOpen()) return selectedSlot.keyPressed(key);
            return false;
        }

        @Override
        public boolean keyReleased(KeyEvent key) {
            if (overlayOpen()) return selectedSlot.keyReleased(key);
            return false;
        }


        public boolean charTyped(CharacterEvent input) {
            if (overlayOpen()) return selectedSlot.charTyped(input);
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (overlayOpen()) return selectedSlot.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return false;
        }

        @Override
        public boolean clickSlot(Slot slot, int button, ContainerInput containerInput, int syncId, int revision) {
            if(CodeClient.MC.player != null && slot.container == CodeClient.MC.player.getInventory()) return false;
            if (containerInput == ContainerInput.PICKUP && !slot.hasItem() && CodeClient.MC.player.containerMenu.getCarried().isEmpty())
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
            private AbstractWidget field;
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
                        new dev.dfonline.codeclient.hypercube.item.Component(),
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
                var manager = mc.gameMode;
                if (manager == null || mc.getConnection() == null) return;
                var currentScreen = mc.gui.screen();
                if (currentScreen instanceof AbstractContainerScreen<?> handledScreen) {
                    var player = mc.player;
                    var sync = handledScreen.getMenu().containerId;
                    manager.handleContainerInput(sync, slot.index, 0, ContainerInput.SWAP, player);
                    mc.getConnection().send(new ServerboundSetCreativeModeSlotPacket(36, stack));
                    manager.handleContainerInput(sync, slot.index, 0, ContainerInput.SWAP, player);
                    manager.handleContainerInput(sync, 54, 0, ContainerInput.QUICK_CRAFT, player);
                    slot.setByPlayer(stack);
                }
            }

            public void close() {
                if (field != null && item != null) {
                    if(field instanceof CustomChestField<?> chestField) item = chestField.item.toStack();
                }
                if(item != null) setSlot(item);
                this.close.run();
            }

            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
//                graphics.getMatrices().translate(0.0F, 0.0F, 900.0F);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("recipe_book/overlay_recipe"), x, y, width, height);
                if (field != null) {
                    field.extractRenderState(graphics, mouseX, mouseY, 0);
                } else {
                    for (var option : options) {
                        graphics.item(option.type, option.x, option.y);
                    }
                }
//                new Identifier("recipe_book/crafting_overlay_highlighted")
//                new Identifier("recipe_book/crafting_overlay")
//                RecipeAlternativesWidget.CRAFTING_OVERLAY_HIGHLIGHTED_TEXTURE : RecipeAlternativesWidget.CRAFTING_OVERLAY_TEXTURE
            }

            public boolean mouseClicked(MouseButtonEvent click, AbstractContainerScreen<?> screen) {
                var mouseX = click.x();
                var mouseY = click.y();
                int x = screenX;
                int y = screenY;
                if (mouseX > this.x + x && mouseY > this.y + y && mouseX < this.x + x + width && mouseY < this.y + y + height) {
                    if (field != null) {
                        boolean b = field.mouseClicked(new MouseButtonEvent(mouseX - x, mouseY - y, click.buttonInfo()), false);
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
                                field = new CustomChestField<>(CodeClient.MC.font, this.x + 5, this.y + 5, this.width - 10, this.height - 10, Component.literal(""), varItem);
                                ((CustomChestField<?>) field).select(true);
                                ((CustomChestField<?>) field).selectAll();
                            }
                            else {
                                this.width = 160 + 10;
                                this.height = 134;
                                field = new ItemSelector(CodeClient.MC.font,this.x + 5, this.y + 5, this.width - 10, this.height - 10,screenX,screenY, itemStack -> {
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

            public boolean keyPressed(KeyEvent key) {
                var keyCode = key.input();
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

            public boolean keyReleased(KeyEvent key) {
                var keyCode = key.input();
                if (field != null) field.keyReleased(key);
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    close();
                    return true;
                }
                return false;
            }

            public boolean charTyped(CharacterEvent charInput) {
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
