package dev.dfonline.codeclient.dev.DevInventory;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.actiondump.ActionDump;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import static dev.dfonline.codeclient.dev.DevInventory.DevInventoryGroup.*;

@Environment(EnvType.CLIENT)
public class DevInventoryScreen extends AbstractInventoryScreen<net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(CodeClient.MOD_ID,"textures/gui/container/dev_inventory/tabs.png");
    private static final String TAB_TEXTURE_PREFIX = "textures/gui/container/creative_inventory/tab_";
    static final SimpleInventory Inventory = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static int selectedTab;
    private double scrollPosition;
    private int scrollHeight = 0;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreNextKey = false;

    public DevInventoryScreen(PlayerEntity player) {
        super(new CreativeInventoryScreen.CreativeScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);
        player.currentScreenHandler = this.handler;
        this.passEvents = true;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.searchBox != null) {
            this.searchBox.tick();
        }
    }

    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if(slot == null) return;
        this.searchBox.setCursorToEnd();
        this.searchBox.setSelectionEnd(0);

        if(slot == this.deleteItemSlot) {
            (this.handler).setCursorStack(ItemStack.EMPTY);
            return;
        }

        if(slot.inventory instanceof SimpleInventory) {
            if(actionType == SlotActionType.PICKUP) {
                if(this.handler.getCursorStack().getItem().equals(Items.AIR)) this.handler.setCursorStack(slot.getStack());
                else this.handler.setCursorStack(Items.AIR.getDefaultStack());
            }
        }
        if(slot.inventory instanceof PlayerInventory) {
            ItemStack slotStack = slot.getStack();
            ItemStack cursorItem = this.handler.getCursorStack();

            slot.setStack(cursorItem);
            this.handler.setCursorStack(slotStack);
            this.handler.syncState();
            this.listener.onSlotUpdate(this.handler,slot.id - 9,slot.getStack());
            CodeClient.LOGGER.info(String.valueOf(slot.id));
//            CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.id,slot.getStack()));
        }
    }
    protected void init() {
        super.init();
        if(this.client == null || this.client.player == null) return;

        try {
            ActionDump.getActionDump();
        }
        catch (IOException | JsonParseException e) {
            CodeClient.LOGGER.error(e.getMessage());
            Utility.sendMessage(Text.literal("Could not parse the ActionDump. Install it using §b/getactiondump colors §cin Node Beta or download a (maybe outdated) version by clicking on this message.")
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://dfonline.dev/public/dbc.json"))), ChatType.FAIL);
        }

        TextRenderer textRenderer = this.textRenderer;
        Objects.requireNonNull(this.textRenderer);
        this.searchBox = new TextFieldWidget(textRenderer, this.x + 82, this.y + 6, 80, 9, Text.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(100);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
        setSelectedTab(0);
        this.client.player.playerScreenHandler.removeListener(this.listener);
        this.listener = new CreativeInventoryListener(this.client);
        this.client.player.playerScreenHandler.addListener(this.listener);
    }

    public void resize(MinecraftClient client, int width, int height) {
        int tab = selectedTab;
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        this.setSelectedTab(tab);
        this.populate();
    }

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

    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];
        if (itemGroup != DevInventoryGroup.INVENTORY) {
            RenderSystem.disableBlend();
            this.textRenderer.draw(matrices, itemGroup.getName(), 8.0F, 6.0F, 4210752);
        }
    }
//
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Integer clicked = getGroupFromMouse(mouseX,mouseY);
            if(clicked != null && clicked != selectedTab) setSelectedTab(clicked);

//            if (selectedTab != ItemGroup.INVENTORY.getIndex() && this.isClickInScrollbar(mouseX, mouseY)) {
//                this.scrolling = this.hasScrollbar();
//                return true;
//            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private Integer getGroupFromMouse(double mouseX, double mouseY) {
        double x = mouseX - (double)this.x;
        double y = mouseY - (double)this.y;
        DevInventoryGroup[] groups = DevInventoryGroup.GROUPS;

        for(int i = 0; i < groups.length; ++i) {
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

        if(this.client == null || this.client.player == null) return;
        if(group == DevInventoryGroup.INVENTORY && this.slots == null) {
            ScreenHandler playerScreenHandler = this.client.player.playerScreenHandler;
            this.slots = ImmutableList.copyOf((this.handler).slots);
            this.handler.slots.clear();
            for (Slot slot : this.slots) {
                if (slot.inventory instanceof PlayerInventory) this.handler.slots.add(slot);
            }

            this.deleteItemSlot = new Slot(Inventory, 0, 173, 112);
            this.handler.slots.add(this.deleteItemSlot);

            int slotSize = 18;

            for(int i = 0; i < 27 && i < playerScreenHandler.slots.size(); ++i) {
                int slotIndex = i + 9;
                int x = 9 + (slotIndex % 9) * slotSize;
                int y = 36 + slotIndex / 9 * slotSize;

                Slot slot = new Slot(playerScreenHandler.slots.get(slotIndex).inventory, slotIndex, x, y);
                this.handler.slots.add(slot);
            }
            return;
        }
        else if(this.slots != null) {
            this.handler.slots.clear();
            this.handler.slots.addAll(this.slots);
            this.slots = null;
        }
        this.scrollPosition = 0;
        populate();
    }
    private void populate() {
        DevInventoryGroup group = GROUPS[selectedTab];
        if(group == INVENTORY) return;
        String text = searchBox.getText();
        if(text.equals("")) text = null;
        List<ItemStack> search = group.searchItems(text);
        if( search == null ) return;
        scrollHeight = Math.max(0,(int) Math.ceil((double) (search.size() - 45) / 9) * 9);
        for (int i = 0; i < 45; i++) {
            int value = i + (int) scrollPosition * 9;
            if(value < search.size()) this.handler.slots.get(i).setStack(search.get(value));
            else this.handler.slots.get(i).setStack(Items.AIR.getDefaultStack());
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Integer hoveredGroup = getGroupFromMouse(mouseX,mouseY);
        if(hoveredGroup != null) {
            DevInventoryGroup group = GROUPS[hoveredGroup];
            if(group != CODE_VAULT || GROUPS[selectedTab] == CODE_VAULT) this.renderTooltip(matrices, GROUPS[hoveredGroup].getName(), mouseX, mouseY);
        }

        if (this.deleteItemSlot != null && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrices, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
    public boolean charTyped(char chr, int modifiers) {
        if(ignoreNextKey) {
            ignoreNextKey = false;
            return true;
        }
        if(searchBox.charTyped(chr, modifiers)) {
            populate();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            int number = keyCode - GLFW.GLFW_KEY_1;
            if(number == -1) number = 9;
            Slot slot = this.handler.slots.get(number);
            if(this.client == null || this.client.player == null) return false;
            this.client.player.setStackInHand(Hand.MAIN_HAND, slot.getStack());
            CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + client.player.getInventory().selectedSlot, slot.getStack()));
            this.ignoreNextKey = true;
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedTab += 7 * ((keyCode == GLFW.GLFW_KEY_DOWN) ? -1 : 1);
            selectedTab %= GROUPS.length;
            if(selectedTab < 0) selectedTab = GROUPS.length + selectedTab;
            setSelectedTab(selectedTab);
        }
        if(keyCode != GLFW.GLFW_KEY_TAB && searchBox.isFocused()) {
            if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.searchBox.setTextFieldFocused(false);
                this.setFocused(null);
                return false;
            }
            searchBox.keyPressed(keyCode, scanCode, modifiers);
            populate();
            return true;
        }
        if(keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT) {
            if((modifiers & GLFW.GLFW_MOD_SHIFT) == 1 || keyCode == GLFW.GLFW_KEY_LEFT) {
                selectedTab = selectedTab - 1;
                if(selectedTab < 0) selectedTab = GROUPS.length + selectedTab;
                setSelectedTab(selectedTab);
            }
            else setSelectedTab((selectedTab + 1) % GROUPS.length);
            return true;
        }
        if(searchBox.active) {
            if(keyCode == GLFW.GLFW_KEY_T) {
                searchBox.setTextFieldFocused(true);
                searchBox.setCursorToEnd();
                searchBox.setSelectionEnd(0);
                ignoreNextKey = true;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];

        for(DevInventoryGroup group : DevInventoryGroup.GROUPS) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (group.getIndex() != selectedTab && group != CODE_VAULT) {
                this.renderTabIcon(matrices, group);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        String texture = "item_search";
        if(!itemGroup.hasSearchBar()) texture = "items";
        if(itemGroup == INVENTORY) texture = "inventory";
        RenderSystem.setShaderTexture(0, new Identifier(TAB_TEXTURE_PREFIX + texture + ".png"));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.renderTabIcon(matrices, itemGroup);

        if(itemGroup.hasSearchBar()) this.searchBox.render(matrices, mouseX, mouseY, delta);
        if (itemGroup != INVENTORY) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            int scrollbarX = this.x + 175;
            int scrollbarY = this.y + 18 ;
            if(scrollHeight == 0) this.drawTexture(matrices, scrollbarX, scrollbarY, 244, 0, 12, 15);
            else this.drawTexture(matrices, scrollbarX, scrollbarY + (95 * (int) (this.scrollPosition * 9) / (scrollHeight)), 232, 0, 12, 15);
        }
        else {
            if(this.client != null && this.client.player != null) InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, (float)(this.x + 88 - mouseX), (float)(this.y + 45 - 30 - mouseY), this.client.player);
        }

    }

    protected boolean isClickInTab(DevInventoryGroup group, double mouseX, double mouseY) {
        int x = group.getDisplayX(0);
        int y = group.getDisplayY(0, this.backgroundHeight);

        return mouseX >= x && mouseX <= (x + TAB_WIDTH)
            && mouseY >= y && mouseY <= (y + TAB_HEIGHT);
    }

    protected void renderTabIcon(MatrixStack matrices, DevInventoryGroup group) {
        boolean isSelected = group.getIndex() == selectedTab;
        boolean isTopRow = group.isTopHalf();
        int column = group.getColumn();
        int mapX = column * TAB_WIDTH;
        int mapY = 0;
        int originX = group.getDisplayX(this.x);
        int originY = group.getDisplayY(this.y, this.backgroundHeight);
        if (isSelected) mapY += 32;
        if(!isTopRow) mapY += 64;

        this.drawTexture(matrices, originX, originY, mapX, mapY, TAB_WIDTH, 32);
        this.itemRenderer.zOffset = 100.0F;
        originX += 6;
        originY += 8 + (isTopRow ? 2 : -2);
        ItemStack itemStack = group.getIcon();
        this.itemRenderer.renderInGuiWithOverrides(itemStack, originX, originY);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, originX, originY);
        this.itemRenderer.zOffset = 0.0F;
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.scrollPosition -= amount;
        if(scrollPosition < 0) scrollPosition = 0;
        if((scrollPosition * 9) > scrollHeight) scrollPosition = (double) scrollHeight / 9;

        populate();
        return true;
    }
}
