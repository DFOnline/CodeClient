package dev.dfonline.codeclient.dev.menu.devinventory;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Searchable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static dev.dfonline.codeclient.dev.menu.devinventory.DevInventoryGroup.*;

@Environment(EnvType.CLIENT)
public class DevInventoryScreen extends HandledScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    static final SimpleInventory Inventory = new SimpleInventory(45);
    private static final Identifier TEXTURE = CodeClient.getId("textures/gui/container/dev_inventory/tabs.png");
//    private static final String TAB_TEXTURE_PREFIX = "container/creative_inventory/tab_";
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static int selectedTab;
    private double scrollPosition;
    private int scrollHeight = 0;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> survivalInventorySwapList;
    private List<Searchable> searchResults;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreNextKey = false;
    private boolean scrolling = false;

    public DevInventoryScreen(ClientPlayerEntity player) {
        super(new CreativeInventoryScreen.CreativeScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);


//        super(player, FeatureSet.empty(), CodeClient.MC.options.getOperatorItemsTab().getValue());
        player.currentScreenHandler = this.handler;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
    }

    public void handledScreenTick() {
        super.handledScreenTick();
//        if (this.searchBox != null) {
//            this.searchBox.tick();
//        }
    }

    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot == null) return;
        this.searchBox.setCursorToEnd(false);
        this.searchBox.setSelectionEnd(0);

        if (slot == this.deleteItemSlot) {
            (this.handler).setCursorStack(ItemStack.EMPTY);
            return;
        }

        if (slot.inventory instanceof SimpleInventory) {
            if (actionType == SlotActionType.PICKUP) {
                if (this.handler.getCursorStack().getItem().equals(Items.AIR))
                    this.handler.setCursorStack(slot.getStack());
                else this.handler.setCursorStack(Items.AIR.getDefaultStack());
            }
        }
        if (slot.inventory instanceof PlayerInventory) {
            ItemStack slotStack = slot.getStack();
            ItemStack cursorItem = this.handler.getCursorStack();

            slot.setStack(cursorItem);
            this.handler.setCursorStack(slotStack);
            this.handler.syncState();
            this.listener.onSlotUpdate(this.handler, slot.id - 9, slot.getStack());
//            CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.id,slot.getStack()));
        }
    }

    protected void init() {
        super.init();
        if (this.client == null || this.client.player == null) return;

        try {
            ActionDump.getActionDump();
        } catch (IOException | JsonParseException e) {
            CodeClient.LOGGER.error(e.getMessage());
            Utility.sendMessage(
                    Text.translatable("codeclient.parse_db", Text.literal("https://github.com/DFOnline/CodeClient/wiki/actiondump").formatted(Formatting.AQUA, Formatting.UNDERLINE))
                            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/DFOnline/CodeClient/wiki/actiondump"))),
                    ChatType.FAIL);
        }

        TextRenderer textRenderer = this.textRenderer;
        Objects.requireNonNull(this.textRenderer);
        this.searchBox = new TextFieldWidget(textRenderer, this.x + 82, this.y + 6, 80, 9, Text.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(100);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
        // TODO: config for defaults on open
        setSelectedTab(6);
        if (Config.getConfig().FocusSearch)
            searchBox.setFocused(true);
        searchBox.setCursorToEnd(false);
        searchBox.setSelectionEnd(0);
        this.client.player.playerScreenHandler.removeListener(this.listener);
        this.listener = new CreativeInventoryListener(this.client);
        this.client.player.playerScreenHandler.addListener(this.listener);
    }

//    public void resize(MinecraftClient client, int width, int height) {
//        int tab = selectedTab;
//        String string = this.searchBox.getText();
//        this.init(client, width, height);
//        this.searchBox.setText(string);
////        this.setSelectedTab(tab);
//        selectedTab = tab;
//        this.scroll();
//    }

    public void removed() {
        super.removed();
        if (this.client != null && this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }
    }

//    public boolean charTyped(char chr, int modifiers) {
//    }

//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//    }

//    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//    }

    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];
        if (itemGroup != DevInventoryGroup.INVENTORY) {
            RenderSystem.disableBlend();
            context.drawText(textRenderer, itemGroup.getName(), 8, 6, 0x404040, false);
        }
    }

    //
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Integer clicked = getGroupFromMouse(mouseX, mouseY);
            if (clicked != null && clicked != selectedTab) setSelectedTab(clicked);

            if (isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int scrollOriginY = this.y + 18;
            int scrollEnd = scrollOriginY + 112;
            double percentThrough = MathHelper.clamp(((float) mouseY - (float) scrollOriginY - 7.5F) / ((float) (scrollEnd - scrollOriginY) - 15.0F), 0, 1);
            this.scrollPosition = ((double) this.scrollHeight / 9) * percentThrough;
            scroll();
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private Integer getGroupFromMouse(double mouseX, double mouseY) {
        double x = mouseX - (double) this.x;
        double y = mouseY - (double) this.y;
        DevInventoryGroup[] groups = DevInventoryGroup.GROUPS;

        for (int i = 0; i < groups.length; ++i) {
            DevInventoryGroup itemGroup = groups[i];
            if (this.isClickInTab(itemGroup, x, y)) {
                return i;
            }
        }
        return null;
    }

    private void setSelectedTab(int tab) {
        searchBox.setText("");
        DevInventoryGroup group = GROUPS[tab];
        searchBox.active = group.hasSearchBar();

        selectedTab = tab;

        if (this.client == null || this.client.player == null) return;
        if (group == DevInventoryGroup.INVENTORY && this.survivalInventorySwapList == null) {
            ScreenHandler playerScreenHandler = this.client.player.playerScreenHandler;
            this.survivalInventorySwapList = ImmutableList.copyOf((this.handler).slots);
            this.handler.slots.clear();
            for (Slot slot : this.survivalInventorySwapList) {
                if (slot.inventory instanceof PlayerInventory) this.handler.slots.add(slot);
            }

            this.deleteItemSlot = new Slot(Inventory, 0, 173, 112);
            this.handler.slots.add(this.deleteItemSlot);

            int slotSize = 18;

            for (int i = 0; i < 27 && i < playerScreenHandler.slots.size(); ++i) {
                int slotIndex = i + 9;
                int x = 9 + (slotIndex % 9) * slotSize;
                int y = 36 + slotIndex / 9 * slotSize;

                Slot slot = new Slot(playerScreenHandler.slots.get(slotIndex).inventory, slotIndex, x, y);
                this.handler.slots.add(slot);
            }
            return;
        } else if (this.survivalInventorySwapList != null) {
            this.handler.slots.clear();
            this.handler.slots.addAll(this.survivalInventorySwapList);
            this.survivalInventorySwapList = null;
        }
        this.scrollPosition = 0;
        populate();
    }

    private void scroll() {
        if (searchResults == null) return;
        scrollHeight = Math.max(0, (int) Math.ceil((double) (searchResults.size() - 45) / 9) * 9);
        for (int i = 0; i < 45; i++) {
            int value = i + (int) scrollPosition * 9;
            if (value < searchResults.size()) this.handler.slots.get(i).setStack(searchResults.get(value).getItem());
            else this.handler.slots.get(i).setStack(Items.AIR.getDefaultStack());
        }
    }

    private void populate() {
        try {
            DevInventoryGroup group = GROUPS[selectedTab];
    //        if(group == INVENTORY) {
    //            searchResults = null;
    //            return;
    //        }
            String text = searchBox.getText();
            if (text.equals("")) text = null;
            searchResults = group.searchItems(text);
            if (searchResults != null) this.scroll();
        } catch (Exception ignored) {
            // Probably caught when we first load the menu.
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        Integer hoveredGroup = getGroupFromMouse(mouseX, mouseY);
        if (hoveredGroup != null) {
            DevInventoryGroup group = GROUPS[hoveredGroup];
            context.drawTooltip(textRenderer, GROUPS[hoveredGroup].getName(), mouseX, mouseY);
        }

        if (this.deleteItemSlot != null && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
            context.drawTooltip(textRenderer, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (ignoreNextKey) {
            ignoreNextKey = false;
            return true;
        }
        if (searchBox.charTyped(chr, modifiers)) {
            populate();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            this.close();
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            int number = keyCode - GLFW.GLFW_KEY_1;
            if (number == -1) number = 9;
            Slot slot = this.handler.slots.get(number);
            if (this.client == null || this.client.player == null) return false;
            this.client.player.setStackInHand(Hand.MAIN_HAND, slot.getStack());
            if (CodeClient.MC.getNetworkHandler() != null) Utility.sendHandItem(slot.getStack());
            this.ignoreNextKey = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedTab += 7 * ((keyCode == GLFW.GLFW_KEY_DOWN) ? -1 : 1);
            selectedTab %= GROUPS.length;
            if (selectedTab < 0) selectedTab = GROUPS.length + selectedTab;
            setSelectedTab(selectedTab);
            return true;
        }
        if (keyCode != GLFW.GLFW_KEY_TAB && searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.searchBox.setFocused(false);
                this.setFocused(null);
                return false;
            }
            searchBox.keyPressed(keyCode, scanCode, modifiers);
            populate();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) == 1 || keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedTab = selectedTab - 1;
                if (selectedTab < 0) selectedTab = GROUPS.length + selectedTab;
                setSelectedTab(selectedTab);
            } else setSelectedTab((selectedTab + 1) % GROUPS.length);
            return true;
        }
        if (searchBox.active) {
            if (CodeClient.MC.options.chatKey.matchesKey(keyCode, scanCode) || KeyBinds.editBind.matchesKey(keyCode, scanCode)) {
                if (keyCode == GLFW.GLFW_KEY_Y) setSelectedTab(SEARCH.getIndex());
                searchBox.setFocused(true);
                searchBox.setCursorToEnd(false);
                searchBox.setSelectionEnd(0);
                ignoreNextKey = true;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];

        for (DevInventoryGroup group : DevInventoryGroup.GROUPS) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderTabIcon(context, group);
        }

        String texture = "item_search";
        if (!itemGroup.hasSearchBar()) texture = "items";
        if (itemGroup == INVENTORY) texture = "inventory";
        context.drawTexture(RenderLayer::getGuiTextured, ItemGroup.getTabTextureId(texture), this.x, this.y, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.renderTabIcon(context, itemGroup);

        if (itemGroup.hasSearchBar()) this.searchBox.render(context, mouseX, mouseY, delta);
        if (itemGroup != INVENTORY) {
            int scrollbarX = this.x + 175;
            int scrollbarY = this.y + 18;
            if (scrollHeight == 0)
                context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, scrollbarX, scrollbarY, 244, 0, 12, 15, 256, 256);
            else
                context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, scrollbarX, scrollbarY + (95 * (int) (this.scrollPosition * 9) / (scrollHeight)), 232, 0, 12, 15, 256, 256);
        } else {
            if (this.client != null && this.client.player != null)
                InventoryScreen.drawEntity(context, this.x + 73, this.y + 6, this.x + 105, this.y + 49, 20, 0.0625F, (float) mouseX, (float) mouseY, this.client.player);
        }

    }

    protected boolean isClickInTab(DevInventoryGroup group, double mouseX, double mouseY) {
        int x = group.getDisplayX(0);
        int y = group.getDisplayY(0, this.backgroundHeight);

        return mouseX >= x && mouseX <= (x + TAB_WIDTH)
                && mouseY >= y && mouseY <= (y + TAB_HEIGHT);
    }

    protected void renderTabIcon(DrawContext context, DevInventoryGroup group) {
        boolean isSelected = group.getIndex() == selectedTab;
        boolean isTopRow = group.isTopHalf();
        int column = group.getColumn();
        int mapX = column * TAB_WIDTH;
        int mapY = 0;
        int originX = group.getDisplayX(this.x);
        int originY = group.getDisplayY(this.y, this.backgroundHeight);
        if (isSelected) mapY += 32;
        if (!isTopRow) mapY += 64;

        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, originX, originY, mapX, mapY, TAB_WIDTH, 32, 256, 256);
//        this.itemRenderer.zOffset = 100.0F;
        originX += 6;
        originY += 8 + (isTopRow ? 2 : -2);
        ItemStack itemStack = group.getIcon();
        context.drawItem(itemStack, originX, originY);
//        this.itemRenderer.zOffset = 0.0F;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!GROUPS[selectedTab].hasSearchBar()) return false;
        this.scrollPosition -= verticalAmount;
        if (scrollPosition < 0) scrollPosition = 0;
        if ((scrollPosition * 9) > scrollHeight) scrollPosition = (double) scrollHeight / 9;

        scroll();
        return true;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        if (!GROUPS[selectedTab].hasSearchBar()) return false;
        int windowX = this.x;
        int windowY = this.y;
        int scrollOriginX = windowX + 175;
        int scrollOriginY = windowY + 18;
        int scrollFarX = scrollOriginX + 14;
        int scrollFarY = scrollOriginY + 112;
        return mouseX >= (double) scrollOriginX && mouseY >= (double) scrollOriginY && mouseX < (double) scrollFarX && mouseY < (double) scrollFarY;
    }
}
