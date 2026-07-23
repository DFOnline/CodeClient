package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.item.BlockTag;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CustomChestMenu extends AbstractContainerScreen<CustomChestHandler> implements MenuAccess<CustomChestHandler> {

    // copy vanilla
    private final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    private final CustomChestNumbers Size;
    private final HashMap<Integer, LayoutElement> widgets = new HashMap<>();
    private final ArrayList<VarItem> varItems = new ArrayList<>();
    private double scroll = 0;
    private boolean doNotUpdate = false;
    private boolean update = true;

    public CustomChestMenu(CustomChestHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.menu.inventory.addListener(ignored -> {
            if(!doNotUpdate) {
                update();
                doNotUpdate = false;
            }
        });
        Size = handler.numbers;
        this.titleLabelY = 4;
        this.inventoryLabelY = 123;
//        this.imageHeight = Size.MENU_HEIGHT;
//        this.imageWidth = Size.MENU_WIDTH;
    }

    @Override
    protected void init() {
        update = true;
        super.init();
    }

    @Override
    protected void containerTick() {
        if (update) {
            update((int) scroll);
            update = false;
        }
        super.containerTick();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.pose().pushMatrix();
        graphics.pose().translate(this.leftPos, this.topPos);
        for (LayoutElement widget : widgets.values()) {
            if (widget instanceof Renderable drawable) {
                drawable.extractRenderState(graphics, mouseX - this.leftPos, mouseY - this.topPos, delta);
            }
        }
        List<Slot> subList = this.getMenu().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);

            final int x = Size.SLOT_X + 1;
            final int y = i * 18 + Size.SLOT_Y + 1;


            if (i + scroll < 27) {

                int relX = mouseX - this.leftPos;
                int relY = mouseY - this.topPos;
                if (
                        relX > x
                                && relX < x + 18
                                && relY > y
                                && relY < y + 18
                ) {
                    hoveredSlot = slot;

                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, x-4, y-4, 24, 24); // draw back
                    extractSlot(graphics, new Slot(slot.container, slot.index, x, y), mouseX, mouseY);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, x-4, y-4, 24, 24); // draw front

                    //drawSlotHighlight(graphics, x, y, -10);
                } else {
                    extractSlot(graphics, new Slot(slot.container, slot.index, x, y), mouseX, mouseY);
                }
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, Size.TEXTURE, x - 1, y - 1, Size.DISABLED_X, 0, 18, 18, Size.TEXTURE_WIDTH, Size.TEXTURE_HEIGHT);
            }
        }

        if (hoveredSlot != null) {
            if (hoveredSlot.hasItem())
                graphics.setTooltipForNextFrame(font, hoveredSlot.getItem(), mouseX, mouseY);
        }
        graphics.pose().popMatrix();
    }

    public void update() {
        update((int) scroll);
    }

    private void update(int previousScroll) {
        Integer focused = null;
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            if (widget instanceof AbstractWidget clickable) {
                if (clickable.isFocused()) focused = (i + previousScroll) - (int) scroll;
            }
        }
        widgets.clear();
        varItems.clear();
        List<Slot> subList = this.getMenu().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            Slot slot = subList.get(i);
            if (!slot.hasItem()) continue;
            ItemStack stack = slot.getItem();
            final int x = Size.WIDGET_X;
            final int y = i * 18 + Size.SLOT_Y;
            VarItem varItem = (VarItems.parse(stack));
            varItems.add(varItem);
            if (i >= Size.WIDGETS) continue;
            if (varItem != null) {
//                TextFieldWidget widget = new TextFieldWidget(textRenderer,x,y,Size.WIDGET_WIDTH,18,Text.of(named.getName()));
//                widget.setMaxLength(10_000);
//                widget.setText(named.getName());
//                widget.setFocused(Objects.equals(i,focused));
                var widget = new CustomChestField<>(font, x, y, Size.WIDGET_WIDTH, 18, Component.nullToEmpty(varItem.getId()), varItem);
                widgets.put(i, widget);
                varItems.add(varItem);
                continue;
            }

            widgets.put(i, new StringWidget(x + 3, y, Size.WIDGET_WIDTH, 16, stack.getHoverName(), font));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (var i : widgets.keySet()) {
            var widget = widgets.get(i);
            if (widget instanceof AbstractWidget click && click.isMouseOver(mouseX - this.leftPos, mouseY - this.topPos)
                    && click.mouseScrolled(mouseX - this.leftPos, mouseY - this.topPos, horizontalAmount, verticalAmount)) {
                updateItem(i);
                return true;
            }
        }
        double prev = scroll;
        scroll = Math.min(Math.max(0, scroll - verticalAmount), 27 - Size.WIDGETS);
        update((int) prev);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        var mouseX = click.x();
        var mouseY = click.y();
        List<Slot> subList = this.getMenu().slots.subList((int) scroll, Math.min((int) scroll + Size.SLOTS, 27));
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);
            final int x = 8;
            final int y = i * 18 - 11 + 25;
            var customSlot = new Slot(slot.container, slot.getContainerSlot(), x, y);
            customSlot.index = slot.index;
            double relX = mouseX - this.leftPos;
            double relY = mouseY - this.topPos;
            if (
                    relX > x
                            && relX < x + 18
                            && relY > y
                            && relY < y + 18
            ) {
                if (click.button() == 2) {
                    this.slotClicked(customSlot, slot.index, click.button(), ContainerInput.CLONE);
                    return true;
                }
                if (click.hasShiftDown()) {
                    this.slotClicked(customSlot, slot.index, click.button(), ContainerInput.QUICK_MOVE);
                    return true;
                }
                this.slotClicked(customSlot, slot.index, click.button(), ContainerInput.PICKUP);
                return true;
            }
        }
        boolean returnValue = false;
        for (var entry : widgets.entrySet()) {
            if (entry.getValue() instanceof AbstractWidget clickable) {
                boolean mouseOver = clickable.isMouseOver(mouseX - this.leftPos, mouseY - this.topPos);
                clickable.setFocused(mouseOver);
                if (mouseOver && clickable.mouseClicked(click, doubled)) {
//                    updateItem(entry.getKey());
                    returnValue = true;
                }
            }
        }
        return returnValue || super.mouseClicked(click, doubled);
    }

    private void updateItems() {
        for (var i : widgets.keySet()) {
            var item = varItems.get(i);
            if (item != null) updateItem(i);
        }
    }

    private void updateItem(int scrollRelativeSlot) {
        if (CodeClient.MC.getConnection() == null) return;
        if (scrollRelativeSlot + scroll > 27) return;
        Slot slot = this.getMenu().slots.subList((int) scroll, (int) scroll + Size.WIDGETS).get(scrollRelativeSlot);
        if (scrollRelativeSlot > widgets.size()) return;
        LayoutElement widget = widgets.get(scrollRelativeSlot);
        if (widget instanceof CustomChestField<?> field) {
            VarItem item = field.item;
            if (item instanceof BlockTag) {
                var hash = HashedStack.create(item.toStack(), CodeClient.MC.getConnection().decoratedHashOpsGenenerator());
                Int2ObjectMap<HashedStack> modified = Int2ObjectMaps.singleton(slot.getContainerSlot(), hash);
                CodeClient.MC.getConnection().send(new ServerboundContainerClickPacket(menu.containerId, menu.incrementStateId(),(short)slot.getContainerSlot(),(byte)0,ContainerInput.PICKUP,modified,hash));
            } else {
                doNotUpdate = true;
                super.slotClicked(slot, slot.index, 0, ContainerInput.SWAP);
                doNotUpdate = true;
                CodeClient.MC.getConnection().send(new ServerboundSetCreativeModeSlotPacket(36, item.toStack()));
                doNotUpdate = true;
                super.slotClicked(slot, slot.index, 0, ContainerInput.SWAP);
                doNotUpdate = true;
                super.slotClicked(slot, 54, 0, ContainerInput.QUICK_CRAFT);
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        var keyCode = input.input();
        var hasShiftDown = input.hasShiftDown();
        if (keyCode <= GLFW.GLFW_KEY_DOWN || keyCode >= GLFW.GLFW_KEY_PAGE_DOWN) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                for (var i : widgets.keySet()) {
                    LayoutElement widget = widgets.get(i);
                    if (widget instanceof AbstractWidget clickable) {
                        if (clickable.isFocused()) {
                            clickable.setFocused(false);
                            updateItem(i);
                            return false;
                        }
                    }
                }
            }
            if(keyCode == GLFW.GLFW_KEY_TAB) {
                for (var i : widgets.keySet()) {
                    LayoutElement widget = widgets.get(i);
                    if (widget instanceof AbstractWidget clickable) {
                        if (clickable.isFocused()) {
                            if(!clickable.keyPressed(input)) {
                                if (widgets.get(hasShiftDown ? --i :   ++i) instanceof CustomChestField<?> next) next.select(!hasShiftDown);
                                clickable.setFocused(false);
                            }
                            return true;
                        }
                    }
                }
                if(widgets.get(hasShiftDown ? widgets.size() - 1 : 0) instanceof CustomChestField<?> next) {
                    next.select(!hasShiftDown);
                }
                return true;
            }
            for (var i : widgets.keySet()) {
                LayoutElement widget = widgets.get(i);
                if (widget instanceof AbstractWidget clickable) {
                    if (clickable.isFocused()) {
                        clickable.keyPressed(input);
                        updateItem(i);
                        return true;
                    }
                }
            }
        }
        boolean up = keyCode == GLFW.GLFW_KEY_UP || CodeClient.MC.options.keyUp.matches(input);
        boolean down = keyCode == GLFW.GLFW_KEY_DOWN || CodeClient.MC.options.keyDown.matches(input);
        boolean pageUp = keyCode == GLFW.GLFW_KEY_PAGE_UP || (up && hasShiftDown);
        boolean pageDown = keyCode == GLFW.GLFW_KEY_PAGE_DOWN || (down && hasShiftDown);
        boolean start = keyCode == GLFW.GLFW_KEY_HOME || (up && input.hasAltDown() && !pageUp);
        boolean end = keyCode == GLFW.GLFW_KEY_END || (down && input.hasAltDown() && !pageDown);
        int prev = (int) scroll;
        if (pageDown) {
            scroll = Math.min(27 - Size.WIDGETS, scroll + Size.WIDGETS);
            update(prev);
        }
        if (pageUp) {
            scroll = Math.max(0, scroll - Size.WIDGETS);
            update(prev);
        }
        if (start) {
            scroll = 0;
            update(prev);
        }
        if (end) {
            scroll = 27 - Size.WIDGETS;
            update(prev);
        }
        if (up) {
            scroll = Math.max(0, scroll - 1);
            update(prev);
        }
        if (down) {
            scroll = Math.min(27 - Size.WIDGETS, scroll + 1);
            update(prev);
        }
        return super.keyPressed(input);
    }


    @Override
    public boolean keyReleased(KeyEvent input) {
        for (var i : widgets.keySet()) {
            LayoutElement widget = widgets.get(i);
            if (widget instanceof AbstractWidget clickable) {
                if (clickable.isFocused()) {
                    clickable.keyReleased(input);
                    updateItem(i);
                    return true;
                }
            }
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        for (var i : widgets.keySet()) {
            LayoutElement widget = widgets.get(i);
            if (widget instanceof AbstractWidget clickable) {
                if (clickable.isFocused()) {
                    clickable.charTyped(input);
                    updateItem(i);
                    return true;
                }
            }
        }
        return super.charTyped(input);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ContainerInput containerInput) {
        super.slotClicked(slot, slotId, button, containerInput);
        update((int) scroll);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) { // TODO(26.2): name is very different, test I got the correct one
//        graphics.getMatrices().push();
//        RenderSystem.enableBlend();
        int centerX = this.width / 2 - (Size.MENU_WIDTH / 2);
        int centerY = this.height / 2 - (Size.MENU_HEIGHT / 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Size.TEXTURE, centerX, centerY, 0, 0, Size.MENU_WIDTH, Size.MENU_HEIGHT, Size.TEXTURE_WIDTH, Size.TEXTURE_HEIGHT);

        boolean disabled = false;
        float scrollProgress = (float) scroll / (27 - Size.WIDGETS);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Size.TEXTURE,
                centerX + Size.SCROLL_POS_X,
                (int) (centerY + Size.SCROLL_POS_Y + scrollProgress * (Size.SCROLL_ROOM - Size.SCROLL_HEIGHT)),
                (disabled ? Size.MENU_WIDTH + Size.SCROLL_WIDTH : Size.MENU_WIDTH),
                0,
                Size.SCROLL_WIDTH,
                Size.SCROLL_HEIGHT,
                Size.TEXTURE_WIDTH,
                Size.TEXTURE_HEIGHT
        );

//        graphics.getMatrices().pop();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
//        super.drawForeground(graphics, mouseX, mouseY);
    }
}
