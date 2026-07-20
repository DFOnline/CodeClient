package dev.dfonline.codeclient.dev.menu.devinventory;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.config.KeyBinds;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Searchable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static dev.dfonline.codeclient.dev.menu.devinventory.DevInventoryGroup.*;

@Environment(EnvType.CLIENT)
public class DevInventoryScreen extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    static final SimpleContainer Inventory = new SimpleContainer(45);
    private static final Identifier TEXTURE = CodeClient.getId("textures/gui/container/dev_inventory/tabs.png");
//    private static final String TAB_TEXTURE_PREFIX = "container/creative_inventory/tab_";
    private static final Component DELETE_ITEM_SLOT_TEXT = Component.translatable("inventory.binSlot");
    private static int selectedTab;
    private double scrollPosition;
    private int scrollHeight = 0;
    private EditBox searchBox;
    @Nullable
    private List<Slot> survivalInventorySwapList;
    private List<Searchable> searchResults;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreNextKey = false;
    private boolean scrolling = false;

    public DevInventoryScreen(LocalPlayer player) {
        super(new CreativeModeInventoryScreen.ItemPickerMenu(player), player.getInventory(), CommonComponents.EMPTY);


//        super(player, FeatureSet.empty(), CodeClient.MC.options.getOperatorItemsTab().getValue());
        player.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
    }

    public void containerTick() {
        super.containerTick();
//        if (this.searchBox != null) {
//            this.searchBox.tick();
//        }
    }

    protected void slotClicked(@Nullable Slot slot, int slotId, int button, ContainerInput containerInput) {
        if (slot == null) return;
        this.searchBox.moveCursorToEnd(false);
        this.searchBox.setHighlightPos(0);

        if (slot == this.deleteItemSlot) {
            (this.menu).setCarried(ItemStack.EMPTY);
            return;
        }

        if (slot.container instanceof SimpleContainer) {
            if (containerInput == ContainerInput.PICKUP) {
                if (this.menu.getCarried().getItem().equals(Items.AIR))
                    this.menu.setCarried(slot.getItem());
                else this.menu.setCarried(Items.AIR.getDefaultInstance());
            }
        }
        if (slot.container instanceof Inventory) {
            ItemStack slotStack = slot.getItem();
            ItemStack cursorItem = this.menu.getCarried();

            slot.setByPlayer(cursorItem);
            this.menu.setCarried(slotStack);
            this.menu.sendAllDataToRemote();
            this.listener.slotChanged(this.menu, slot.index - 9, slot.getItem());
//            CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.id,slot.getStack()));
        }
    }

    protected void init() {
        super.init();
        if (this.minecraft == null || this.minecraft.player == null) return;

        try {
            ActionDump.getActionDump();
        } catch (IOException | JsonParseException e) {
            CodeClient.LOGGER.error(e.getMessage());
            Utility.sendMessage(
                    Component.translatable("codeclient.parse_db", Component.literal("https://github.com/DFOnline/CodeClient/wiki/actiondump").withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
                            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/DFOnline/CodeClient/wiki/actiondump")))),
                    ChatType.FAIL);
        }

        Objects.requireNonNull(this.font);
        this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(100);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(CommonColors.WHITE);
        this.addWidget(this.searchBox);
        // TODO: config for defaults on open
        setSelectedTab(6);
        if (Config.getConfig().FocusSearch)
            searchBox.setFocused(true);
        searchBox.moveCursorToEnd(false);
        searchBox.setHighlightPos(0);
        this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        this.listener = new CreativeInventoryListener(this.minecraft);
        this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
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
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
    }

//    public boolean charTyped(char chr, int modifiers) {
//    }

//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//    }

//    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//    }

    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];
        if (itemGroup != DevInventoryGroup.INVENTORY) {
            context.drawString(this.font, itemGroup.getName(), 8, 6, CommonColors.DARK_GRAY, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            Integer clicked = getGroupFromMouse(click.x(), click.y());
            if (clicked != null && clicked != selectedTab) setSelectedTab(clicked);

            if (isClickInScrollbar(click.x(), click.y())) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (this.scrolling) {
            int scrollOriginY = this.topPos + 18;
            int scrollEnd = scrollOriginY + 112;
            double percentThrough = Mth.clamp(((float) click.y() - (float) scrollOriginY - 7.5F) / ((float) (scrollEnd - scrollOriginY) - 15.0F), 0, 1);
            this.scrollPosition = ((double) this.scrollHeight / 9) * percentThrough;
            scroll();
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    private Integer getGroupFromMouse(double mouseX, double mouseY) {
        double x = mouseX - (double) this.leftPos;
        double y = mouseY - (double) this.topPos;
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
        searchBox.setValue("");
        DevInventoryGroup group = GROUPS[tab];
        searchBox.active = group.hasSearchBar();

        selectedTab = tab;

        if (this.minecraft == null || this.minecraft.player == null) return;
        if (group == DevInventoryGroup.INVENTORY && this.survivalInventorySwapList == null) {
            AbstractContainerMenu playerScreenHandler = this.minecraft.player.inventoryMenu;
            this.survivalInventorySwapList = ImmutableList.copyOf((this.menu).slots);
            this.menu.slots.clear();
            for (Slot slot : this.survivalInventorySwapList) {
                if (slot.container instanceof Inventory) this.menu.slots.add(slot);
            }

            this.deleteItemSlot = new Slot(Inventory, 0, 173, 112);
            this.menu.slots.add(this.deleteItemSlot);

            int slotSize = 18;

            for (int i = 0; i < 27 && i < playerScreenHandler.slots.size(); ++i) {
                int slotIndex = i + 9;
                int x = 9 + (slotIndex % 9) * slotSize;
                int y = 36 + slotIndex / 9 * slotSize;

                Slot slot = new Slot(playerScreenHandler.slots.get(slotIndex).container, slotIndex, x, y);
                this.menu.slots.add(slot);
            }
            return;
        } else if (this.survivalInventorySwapList != null) {
            this.menu.slots.clear();
            this.menu.slots.addAll(this.survivalInventorySwapList);
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
            if (value < searchResults.size()) this.menu.slots.get(i).setByPlayer(searchResults.get(value).getItem());
            else this.menu.slots.get(i).setByPlayer(Items.AIR.getDefaultInstance());
        }
    }

    private void populate() {
        try {
            DevInventoryGroup group = GROUPS[selectedTab];
    //        if(group == INVENTORY) {
    //            searchResults = null;
    //            return;
    //        }
            String text = searchBox.getValue();
            if (text.equals("")) text = null;
            searchResults = group.searchItems(text);
            if (searchResults != null) this.scroll();
        } catch (Exception ignored) {
            // Probably caught when we first load the menu.
        }
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        Integer hoveredGroup = getGroupFromMouse(mouseX, mouseY);
        if (hoveredGroup != null) {
            DevInventoryGroup group = GROUPS[hoveredGroup];
            context.setTooltipForNextFrame(font, GROUPS[hoveredGroup].getName(), mouseX, mouseY);
        }

        if (this.deleteItemSlot != null && this.isHovering(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
            context.setTooltipForNextFrame(font, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (ignoreNextKey) {
            ignoreNextKey = false;
            return true;
        }
        if (searchBox.charTyped(input)) {
            populate();
            return true;
        }
        return false;
    }


    @Override
    public boolean keyPressed(KeyEvent input) {
        var keyCode = input.input();
        if (keyCode == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            this.onClose();
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            int number = keyCode - GLFW.GLFW_KEY_1;
            if (number == -1) number = 9;
            Slot slot = this.menu.slots.get(number);
            if (this.minecraft == null || this.minecraft.player == null) return false;
            this.minecraft.player.setItemInHand(InteractionHand.MAIN_HAND, slot.getItem());
            if (CodeClient.MC.getConnection() != null) Utility.sendHandItem(slot.getItem());
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
            searchBox.keyPressed(input);
            populate();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT) {
            if ((input.modifiers() & GLFW.GLFW_MOD_SHIFT) == 1 || keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedTab = selectedTab - 1;
                if (selectedTab < 0) selectedTab = GROUPS.length + selectedTab;
                setSelectedTab(selectedTab);
            } else setSelectedTab((selectedTab + 1) % GROUPS.length);
            return true;
        }
        if (searchBox.active) {
            if (CodeClient.MC.options.keyChat.matches(input) || KeyBinds.editBind.matches(input)) {
                if (keyCode == GLFW.GLFW_KEY_Y) setSelectedTab(SEARCH.getIndex());
                searchBox.setFocused(true);
                searchBox.moveCursorToEnd(false);
                searchBox.setHighlightPos(0);
                ignoreNextKey = true;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];

        for (DevInventoryGroup group : DevInventoryGroup.GROUPS) {
//            RenderSystem.setShaderTexture(0, TEXTURE);
            this.renderTabIcon(context, group);
        }

        String texture = "item_search";
        if (!itemGroup.hasSearchBar()) texture = "items";
        if (itemGroup == INVENTORY) texture = "inventory";
        context.blit(RenderPipelines.GUI_TEXTURED, CreativeModeTab.createTextureLocation(texture), this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        this.renderTabIcon(context, itemGroup);

        if (itemGroup.hasSearchBar()) this.searchBox.render(context, mouseX, mouseY, delta);
        if (itemGroup != INVENTORY) {
            int scrollbarX = this.leftPos + 175;
            int scrollbarY = this.topPos + 18;
            if (scrollHeight == 0)
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, scrollbarX, scrollbarY, 244, 0, 12, 15, 256, 256);
            else
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, scrollbarX, scrollbarY + (95 * (int) (this.scrollPosition * 9) / (scrollHeight)), 232, 0, 12, 15, 256, 256);
        } else {
            if (this.minecraft != null && this.minecraft.player != null)
                InventoryScreen.renderEntityInInventoryFollowsMouse(context, this.leftPos + 73, this.topPos + 6, this.leftPos + 105, this.topPos + 49, 20, 0.0625F, (float) mouseX, (float) mouseY, this.minecraft.player);
        }
    }

    protected boolean isClickInTab(DevInventoryGroup group, double mouseX, double mouseY) {
        int x = group.getDisplayX(0);
        int y = group.getDisplayY(0, this.imageHeight);

        return mouseX >= x && mouseX <= (x + TAB_WIDTH)
                && mouseY >= y && mouseY <= (y + TAB_HEIGHT);
    }

    protected void renderTabIcon(GuiGraphics context, DevInventoryGroup group) {
        boolean isSelected = group.getIndex() == selectedTab;
        boolean isTopRow = group.isTopHalf();
        int column = group.getColumn();
        int mapX = column * TAB_WIDTH;
        int mapY = 0;
        int originX = group.getDisplayX(this.leftPos);
        int originY = group.getDisplayY(this.topPos, this.imageHeight);
        if (isSelected) mapY += 32;
        if (!isTopRow) mapY += 64;

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, originX, originY, mapX, mapY, TAB_WIDTH, 32, 256, 256);
//        this.itemRenderer.zOffset = 100.0F;
        originX += 6;
        originY += 8 + (isTopRow ? 2 : -2);
        ItemStack itemStack = group.getIcon();
        context.renderItem(itemStack, originX, originY);
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
        int windowX = this.leftPos;
        int windowY = this.topPos;
        int scrollOriginX = windowX + 175;
        int scrollOriginY = windowY + 18;
        int scrollFarX = scrollOriginX + 14;
        int scrollFarY = scrollOriginY + 112;
        return mouseX >= (double) scrollOriginX && mouseY >= (double) scrollOriginY && mouseX < (double) scrollFarX && mouseY < (double) scrollFarY;
    }
}
