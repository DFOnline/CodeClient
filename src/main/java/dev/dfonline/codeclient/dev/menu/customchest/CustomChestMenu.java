package dev.dfonline.codeclient.dev.menu.customchest;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.hypercube.item.BlockTag;
import dev.dfonline.codeclient.hypercube.item.VarItem;
import dev.dfonline.codeclient.hypercube.item.VarItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomChestMenu extends HandledScreen<CustomChestHandler> implements ScreenHandlerProvider<CustomChestHandler> {

    // copy vanilla
    private final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/slot_highlight_back");
    private final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/slot_highlight_front");

    private final CustomChestNumbers Size;
    private final HashMap<Integer, Widget> widgets = new HashMap<>();
    private final ArrayList<VarItem> varItems = new ArrayList<>();
    private double scroll = 0;
    private boolean doNotUpdate = false;
    private boolean update = true;

    public CustomChestMenu(CustomChestHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler.inventory.addListener(ignored -> {
            if(!doNotUpdate) {
                update();
                doNotUpdate = false;
            }
        });
        Size = handler.numbers;
        this.titleY = 4;
        this.playerInventoryTitleY = 123;
        this.backgroundHeight = Size.MENU_HEIGHT;
        this.backgroundWidth = Size.MENU_WIDTH;
    }

    @Override
    protected void init() {
        update = true;
        super.init();
    }

    @Override
    protected void handledScreenTick() {
        if (update) {
            update((int) scroll);
            update = false;
        }
        super.handledScreenTick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0);
        for (Widget widget : widgets.values()) {
            if (widget instanceof Drawable drawable) {
                drawable.render(context, mouseX - this.x, mouseY - this.y, delta);
            }
        }
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);

            final int x = Size.SLOT_X + 1;
            final int y = i * 18 + Size.SLOT_Y + 1;


            if (i + scroll < 27) {

                int relX = mouseX - this.x;
                int relY = mouseY - this.y;
                if (
                        relX > x
                                && relX < x + 18
                                && relY > y
                                && relY < y + 18
                ) {
                    focusedSlot = slot;

                    context.drawGuiTexture(RenderLayer::getGuiTextured, SLOT_HIGHLIGHT_BACK_TEXTURE, x-4, y-4, 24, 24); // draw back
                    drawSlot(context, new Slot(slot.inventory, slot.id, x, y));
                    context.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_TEXTURE, x-4, y-4, 24, 24); // draw front

                    //drawSlotHighlight(context, x, y, -10);
                } else {
                    drawSlot(context, new Slot(slot.inventory, slot.id, x, y));
                }
            } else {
                context.drawTexture(RenderLayer::getGuiTextured, Size.TEXTURE, x - 1, y - 1, Size.DISABLED_X, 0, 18, 18, Size.TEXTURE_WIDTH, Size.TEXTURE_HEIGHT);
            }
        }

        if (focusedSlot != null) {
            if (focusedSlot.hasStack())
                context.drawItemTooltip(textRenderer, focusedSlot.getStack(), mouseX - this.x, mouseY - this.y);
        }
        context.getMatrices().pop();
    }

    public void update() {
        update((int) scroll);
    }

    private void update(int previousScroll) {
        Integer focused = null;
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            if (widget instanceof ClickableWidget clickable) {
                if (clickable.isFocused()) focused = (i + previousScroll) - (int) scroll;
            }
        }
        widgets.clear();
        varItems.clear();
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + Size.SLOTS);
        for (int i = 0; i < subList.size(); i++) {
            Slot slot = subList.get(i);
            if (!slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
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
                var widget = new CustomChestField<>(textRenderer, x, y, Size.WIDGET_WIDTH, 18, Text.of(varItem.getId()), varItem);
                widgets.put(i, widget);
                varItems.add(varItem);
                continue;
            }

            widgets.put(i, new TextWidget(x + 3, y, Size.WIDGET_WIDTH, 16, stack.getName(), textRenderer).alignLeft());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (var i : widgets.keySet()) {
            var widget = widgets.get(i);
            if (widget instanceof ClickableWidget click && click.isMouseOver(mouseX - this.x, mouseY - this.y)
                    && click.mouseScrolled(mouseX - this.x, mouseY - this.y, horizontalAmount, verticalAmount)) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Slot> subList = this.getScreenHandler().slots.subList((int) scroll, Math.min((int) scroll + Size.SLOTS, 27));
        for (int i = 0; i < subList.size(); i++) {
            var slot = subList.get(i);
            final int x = 8;
            final int y = i * 18 - 11 + 25;
            var customSlot = new Slot(slot.inventory, slot.getIndex(), x, y);
            customSlot.id = slot.id;
            double relX = mouseX - this.x;
            double relY = mouseY - this.y;
            if (
                    relX > x
                            && relX < x + 18
                            && relY > y
                            && relY < y + 18
            ) {
                if (button == 2) {
                    this.onMouseClick(customSlot, slot.id, button, SlotActionType.CLONE);
                    return true;
                }
                if (hasShiftDown()) {
                    this.onMouseClick(customSlot, slot.id, button, SlotActionType.QUICK_MOVE);
                    return true;
                }
                this.onMouseClick(customSlot, slot.id, button, SlotActionType.PICKUP);
                return true;
            }
        }
        boolean returnValue = false;
        for (var entry : widgets.entrySet()) {
            if (entry.getValue() instanceof ClickableWidget clickable) {
                boolean mouseOver = clickable.isMouseOver(mouseX - this.x, mouseY - this.y);
                clickable.setFocused(mouseOver);
                if (mouseOver && clickable.mouseClicked(mouseX - this.x, mouseY - this.y, button)) {
//                    updateItem(entry.getKey());
                    returnValue = true;
                }
            }
        }
        return returnValue || super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateItems() {
        for (var i : widgets.keySet()) {
            var item = varItems.get(i);
            if (item != null) updateItem(i);
        }
    }

    private void updateItem(int scrollRelativeSlot) {
        if (CodeClient.MC.getNetworkHandler() == null) return;
        if (scrollRelativeSlot + scroll > 27) return;
        Slot slot = this.getScreenHandler().slots.subList((int) scroll, (int) scroll + Size.WIDGETS).get(scrollRelativeSlot);
        if (scrollRelativeSlot > widgets.size()) return;
        Widget widget = widgets.get(scrollRelativeSlot);
        if (widget instanceof CustomChestField<?> field) {
            VarItem item = field.item;
            if (item instanceof BlockTag) {
                Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
                CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.nextRevision(), slot.getIndex(), 0, SlotActionType.PICKUP, item.toStack(), int2ObjectMap));
            } else {
                doNotUpdate = true;
                super.onMouseClick(slot, slot.id, 0, SlotActionType.SWAP);
                doNotUpdate = true;
                CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36, item.toStack()));
                doNotUpdate = true;
                super.onMouseClick(slot, slot.id, 0, SlotActionType.SWAP);
                doNotUpdate = true;
                super.onMouseClick(slot, 54, 0, SlotActionType.QUICK_CRAFT);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode <= GLFW.GLFW_KEY_DOWN || keyCode >= GLFW.GLFW_KEY_PAGE_DOWN) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                for (var i : widgets.keySet()) {
                    Widget widget = widgets.get(i);
                    if (widget instanceof ClickableWidget clickable) {
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
                    Widget widget = widgets.get(i);
                    if (widget instanceof ClickableWidget clickable) {
                        if (clickable.isFocused()) {
                            if(!clickable.keyPressed(keyCode, scanCode, modifiers)) {
                                if (widgets.get(hasShiftDown() ? --i :   ++i) instanceof CustomChestField<?> next) next.select(!hasShiftDown());
                                clickable.setFocused(false);
                            }
                            return true;
                        }
                    }
                }
                if(widgets.get(hasShiftDown() ? widgets.size() - 1 : 0) instanceof CustomChestField<?> next) {
                    next.select(!hasShiftDown());
                }
                return true;
            }
            for (var i : widgets.keySet()) {
                Widget widget = widgets.get(i);
                if (widget instanceof ClickableWidget clickable) {
                    if (clickable.isFocused()) {
                        clickable.keyPressed(keyCode, scanCode, modifiers);
                        updateItem(i);
                        return true;
                    }
                }
            }
        }
        boolean up = keyCode == GLFW.GLFW_KEY_UP || CodeClient.MC.options.forwardKey.matchesKey(keyCode, scanCode);
        boolean down = keyCode == GLFW.GLFW_KEY_DOWN || CodeClient.MC.options.backKey.matchesKey(keyCode, scanCode);
        boolean pageUp = keyCode == GLFW.GLFW_KEY_PAGE_UP || (up && hasShiftDown());
        boolean pageDown = keyCode == GLFW.GLFW_KEY_PAGE_DOWN || (down && hasShiftDown());
        boolean start = keyCode == GLFW.GLFW_KEY_HOME || (up && hasAltDown() && !pageUp);
        boolean end = keyCode == GLFW.GLFW_KEY_END || (down && hasAltDown() && !pageDown);
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (var i : widgets.keySet()) {
            Widget widget = widgets.get(i);
            if (widget instanceof ClickableWidget clickable) {
                if (clickable.isFocused()) {
                    clickable.keyReleased(keyCode, scanCode, modifiers);
                    updateItem(i);
                    return true;
                }
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (var i : widgets.keySet()) {
            Widget widget = widgets.get(i);
            if (widget instanceof ClickableWidget clickable) {
                if (clickable.isFocused()) {
                    clickable.charTyped(chr, modifiers);
                    updateItem(i);
                    return true;
                }
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        update((int) scroll);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.getMatrices().push();
        RenderSystem.enableBlend();
        int centerX = this.width / 2 - (Size.MENU_WIDTH / 2);
        int centerY = this.height / 2 - (Size.MENU_HEIGHT / 2);
        context.drawTexture(RenderLayer::getGuiTextured, Size.TEXTURE, centerX, centerY, 0, 0, Size.MENU_WIDTH, Size.MENU_HEIGHT, Size.TEXTURE_WIDTH, Size.TEXTURE_HEIGHT);

        boolean disabled = false;
        float scrollProgress = (float) scroll / (27 - Size.WIDGETS);
        context.drawTexture(RenderLayer::getGuiTextured, Size.TEXTURE,
                centerX + Size.SCROLL_POS_X,
                (int) (centerY + Size.SCROLL_POS_Y + scrollProgress * (Size.SCROLL_ROOM - Size.SCROLL_HEIGHT)),
                (disabled ? Size.MENU_WIDTH + Size.SCROLL_WIDTH : Size.MENU_WIDTH),
                0,
                Size.SCROLL_WIDTH,
                Size.SCROLL_HEIGHT,
                Size.TEXTURE_WIDTH,
                Size.TEXTURE_HEIGHT
        );

        context.getMatrices().pop();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
//        super.drawForeground(context, mouseX, mouseY);
    }
}
