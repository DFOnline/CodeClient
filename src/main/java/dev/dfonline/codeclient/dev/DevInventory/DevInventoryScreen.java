package dev.dfonline.codeclient.dev.DevInventory;

import blue.endless.jankson.JsonArray;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dfonline.codeclient.CodeClient;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static dev.dfonline.codeclient.dev.DevInventory.DevInventoryGroup.*;

@Environment(EnvType.CLIENT)
public class DevInventoryScreen extends AbstractInventoryScreen<net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(CodeClient.MOD_ID,"textures/gui/container/dev_inventory/tabs.png");
    private static final String TAB_TEXTURE_PREFIX = "textures/gui/container/creative_inventory/tab_";
    static final SimpleInventory Inventory = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static int selectedTab;
    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Set<TagKey<Item>> searchResultTags = new HashSet();
    private List<ItemStack> itemList;

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
//
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
                this.handler.setCursorStack(slot.getStack());
            }
        }
        if(slot.inventory instanceof PlayerInventory) {
            ItemStack slotStack = slot.getStack();
            ItemStack cursorItem = this.handler.getCursorStack();

            slot.setStack(cursorItem);
            this.handler.setCursorStack(slotStack);
        }
//
//        boolean bl = actionType == SlotActionType.QUICK_MOVE;
//        actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
//        ItemStack itemStack;
//        if (slot == null && selectedTab != ItemGroup.INVENTORY.getIndex() && actionType != SlotActionType.QUICK_CRAFT) {
//            if (!this.handler.getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
//                if (button == 0) {
//                    this.client.player.dropItem((this.handler).getCursorStack(), true);
//                    this.client.interactionManager.dropCreativeStack((this.handler).getCursorStack());
//                    (this.handler).setCursorStack(ItemStack.EMPTY);
//                }
//
//                if (button == 1) {
//                    itemStack = (this.handler).getCursorStack().split(1);
//                    this.client.player.dropItem(itemStack, true);
//                    this.client.interactionManager.dropCreativeStack(itemStack);
//                }
//            }
//        } else {
//            if (slot != null && !slot.canTakeItems(this.client.player)) {
//                return;
//            }
//
//            if (slot == this.deleteItemSlot && bl) {
//                for(int i = 0; i < this.client.player.playerScreenHandler.getStacks().size(); ++i) {
//                    this.client.interactionManager.clickCreativeStack(ItemStack.EMPTY, i);
//                }
//            } else {
//                ItemStack itemStack2;
//                if (selectedTab == ItemGroup.INVENTORY.getIndex()) {

//                    } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
//                        itemStack = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
//                        itemStack2 = slot.getStack();
//                        this.client.player.dropItem(itemStack, true);
//                        this.client.interactionManager.dropCreativeStack(itemStack);
//                        this.client.interactionManager.clickCreativeStack(itemStack2, ((CreativeSlot)slot).slot.id);
//                    } else if (actionType == SlotActionType.THROW && !(this.handler).getCursorStack().isEmpty()) {
//                        this.client.player.dropItem((this.handler).getCursorStack(), true);
//                        this.client.interactionManager.dropCreativeStack((this.handler).getCursorStack());
//                        (this.handler).setCursorStack(ItemStack.EMPTY);
//                    } else {
//                        this.client.player.playerScreenHandler.onSlotClick(slot == null ? slotId : ((CreativeSlot) slot).slot.id, button, actionType, this.client.player);
//                        this.client.player.playerScreenHandler.sendContentUpdates();
//                    }
//                } else if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
//                    itemStack = (this.handler).getCursorStack();
//                    itemStack2 = slot.getStack();
//                    ItemStack itemStack3;
//                    if (actionType == SlotActionType.SWAP) {
//                        if (!itemStack2.isEmpty()) {
//                            itemStack3 = itemStack2.copy();
//                            itemStack3.setCount(itemStack3.getMaxCount());
//                            this.client.player.getInventory().setStack(button, itemStack3);
//                            this.client.player.playerScreenHandler.sendContentUpdates();
//                        }
//
//                        return;
//                    }
//
//                    if (actionType == SlotActionType.CLONE) {
//                        if ((this.handler).getCursorStack().isEmpty() && slot.hasStack()) {
//                            itemStack3 = slot.getStack().copy();
//                            itemStack3.setCount(itemStack3.getMaxCount());
//                            (this.handler).setCursorStack(itemStack3);
//                        }
//
//                        return;
//                    }
//
//                    if (actionType == SlotActionType.THROW) {
//                        if (!itemStack2.isEmpty()) {
//                            itemStack3 = itemStack2.copy();
//                            itemStack3.setCount(button == 0 ? 1 : itemStack3.getMaxCount());
//                            this.client.player.dropItem(itemStack3, true);
//                            this.client.interactionManager.dropCreativeStack(itemStack3);
//                        }
//
//                        return;
//                    }
//
//                    if (!itemStack.isEmpty() && !itemStack2.isEmpty() && itemStack.isItemEqualIgnoreDamage(itemStack2) && ItemStack.areNbtEqual(itemStack, itemStack2)) {
//                        if (button == 0) {
//                            if (bl) {
//                                itemStack.setCount(itemStack.getMaxCount());
//                            } else if (itemStack.getCount() < itemStack.getMaxCount()) {
//                                itemStack.increment(1);
//                            }
//                        } else {
//                            itemStack.decrement(1);
//                        }
//                    } else if (!itemStack2.isEmpty() && itemStack.isEmpty()) {
//                        (this.handler).setCursorStack(itemStack2.copy());
//                        itemStack = (this.handler).getCursorStack();
//                        if (bl) {
//                            itemStack.setCount(itemStack.getMaxCount());
//                        }
//                    } else if (button == 0) {
//                        (this.handler).setCursorStack(ItemStack.EMPTY);
//                    } else {
//                        (this.handler).getCursorStack().decrement(1);
//                    }
//                } else if (this.handler != null) {
//                    itemStack = slot == null ? ItemStack.EMPTY : (this.handler).getSlot(slot.id).getStack();
//                    (this.handler).onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
//                    if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
//                        for(int j = 0; j < 9; ++j) {
//                            this.client.interactionManager.clickCreativeStack((this.handler).getSlot(45 + j).getStack(), 36 + j);
//                        }
//                    } else if (slot != null) {
//                        itemStack2 = (this.handler).getSlot(slot.id).getStack();
//                        this.client.interactionManager.clickCreativeStack(itemStack2, slot.id - (this.handler).slots.size() + 9 + 36);
//                        int k = 45 + button;
//                        if (actionType == SlotActionType.SWAP) {
//                            this.client.interactionManager.clickCreativeStack(itemStack, k - (this.handler).slots.size() + 9 + 36);
//                        } else if (actionType == SlotActionType.THROW && !itemStack.isEmpty()) {
//                            ItemStack itemStack4 = itemStack.copy();
//                            itemStack4.setCount(button == 0 ? 1 : itemStack4.getMaxCount());
//                            this.client.player.dropItem(itemStack4, true);
//                            this.client.interactionManager.dropCreativeStack(itemStack4);
//                        }
//
//                        this.client.player.playerScreenHandler.sendContentUpdates();
//                    }
//                }
//            }
//        }
//
    }
    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
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
//        if (!this.searchBox.getText().isEmpty()) {
//            this.search();
//        }

    }
//
    public void removed() {
        super.removed();
        if (this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }
        this.client.keyboard.setRepeatEvents(false);
    }

//    public boolean charTyped(char chr, int modifiers) {

//    }

//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

//    }

//    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//    }

//    private void search() {
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
        selectedTab = tab;

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
//                int xPos;
//                int l;
//                int m;
//                int row;
//                int yPos;
//                if (slotIndex >= 5 && slotIndex < 9) {
//                    l = slotIndex - 5;
//                    m = l / 2;
//                    row = l % 2;
//                    xPos = 54 + m * 54;
//                    yPos = 6 + row * 27;
//                } else if (slotIndex < 5) {
//                    xPos = -2000;
//                    yPos = -2000;
//                } else if (slotIndex == 45) {
//                    xPos = 35;
//                    yPos = 20;
//                } else {
//                    l = slotIndex - 9;
//                    m = l % 9;
//                    row = l / 9;
//                    xPos = 9 + m * 18;
//                    if (slotIndex >= 36) {
//                        yPos = 112;
//                    } else {
//                        yPos = 54 + row * 18;
//                    }
//                }

                Slot slot = new Slot(playerScreenHandler.slots.get(slotIndex).inventory, slotIndex, x, y);
                this.handler.slots.add(slot);
            }

        }
        else if(this.slots != null) {
            this.handler.slots.clear();
            this.handler.slots.addAll(this.slots);
            this.slots = null;
        }
        if(group == OTHERS) {
            try {
                this.handler.slots.get(0).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:book\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"txt\",\"data\":{\"name\":\"text\"}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"italic\":false,\"color\":\"aqua\",\"text\":\"text\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Type the text in chat while\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"holding this item.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"E.g. \\'\"},{\"italic\":false,\"color\":\"white\",\"text\":\"Sample Text\"},{\"italic\":false,\"color\":\"gray\",\"text\":\"\\' or \\'\"},{\"italic\":false,\"color\":\"white\",\"text\":\"&cRed\"},{\"italic\":false,\"color\":\"gray\",\"text\":\"\\'\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"aqua\",\"text\":\"Text\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(1).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:nautilus_shell\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"snd\",\"data\":{\"sound\":\"Pling\",\"pitch\":1.0,\"vol\":2.0}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"italic\":false,\"color\":\"blue\",\"text\":\"sound\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click this item to select\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"a sound effect.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Shift right-click to select\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"a variant of the sound.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"To add a pitch & volume, type\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"them in one chat line, in order.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Left-click to preview it.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"blue\",\"text\":\"Sound\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(2).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:prismarine_shard\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"vec\",\"data\":{\"x\":0.0,\"y\":0.0,\"z\":0.0}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#2AFFAA\",\"text\":\"vector\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Use the /vector command or\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"type \"},{\"italic\":false,\"color\":\"aqua\",\"text\":\"\\\\\"<x> <y> <z>\\\\\"\"},{\"italic\":false,\"color\":\"gray\",\"text\":\" to set the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"components. Left-click to\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"preview it.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#2AFFAA\",\"text\":\"Vector\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(3).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:paper\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"loc\",\"data\":{\"isBlock\":false,\"loc\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"pitch\":0.0,\"yaw\":0.0}}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"italic\":false,\"color\":\"green\",\"text\":\"location\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click this item to set it\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to your current location.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Left-click to set it to a block.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Shift left-click to teleport to it.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"green\",\"text\":\"Location\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(4).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:slime_ball\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"num\",\"data\":{\"name\":\"0\"}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"italic\":false,\"color\":\"red\",\"text\":\"number\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Type a number in chat while\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"holding this item.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"E.g. \"},{\"italic\":false,\"color\":\"white\",\"text\":\"4\"},{\"italic\":false,\"color\":\"gray\",\"text\":\" or \"},{\"italic\":false,\"color\":\"white\",\"text\":\"1.25\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"red\",\"text\":\"Number\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(5).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:white_dye\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"part\",\"data\":{\"particle\":\"Cloud\",\"cluster\":{\"amount\":1,\"horizontal\":0.0,\"vertical\":0.0},\"data\":{\"x\":1.0,\"y\":0.0,\"z\":0.0,\"motionVariation\":100}}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#AA55FF\",\"text\":\"particle\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click this item to select\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"a particle effect.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Use the /particle command\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to change its parameters.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Left-click to preview it.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#AA55FF\",\"text\":\"Particle\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(6).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:dragon_breath\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"pot\",\"data\":{\"pot\":\"Speed\",\"dur\":1000000,\"amp\":0}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item used for \"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FF557F\",\"text\":\"potion\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"effect parameters.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click this item to select\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"a potion effect.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Use the /potion command or\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"type \"},{\"italic\":false,\"color\":\"aqua\",\"text\":\"\\\\\"<amplifier> [duration]\\\\\"\"},{\"italic\":false,\"color\":\"gray\",\"text\":\"\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to set its options. Left-click\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to preview it.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FF557F\",\"text\":\"Potion Effect\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(9).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:magma_cream\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"var\",\"data\":{\"name\":\"&eVariable\",\"scope\":\"unsaved\"}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"A value item name that refers\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to an internally stored value.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"The value can be set using the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"white\",\"text\":\"Set Variable\"},{\"italic\":false,\"color\":\"gray\",\"text\":\" block.\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Type a variable name in chat\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"while holding this item.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Shift right-click for options.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"yellow\",\"text\":\"Variable\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(10).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:name_tag\",tag:{CustomModelData:5000,PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"g_val\",\"data\":{\"type\":\"Location\",\"target\":\"Default\"}}'},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"An automatically set value\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"which is set based on the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"game\\'s current conditions\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"(e.g. a player\\'s location).\"}],\"text\":\"\"}','{\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"light_purple\",\"text\":\"How to set:\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click to select a game\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"value and its target.\"}],\"text\":\"\"}'],Name:'{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FFD47F\",\"text\":\"Game Value\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(8).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:written_book\",tag:{CustomModelData:0,HideFlags:127,PublicBukkitValues:{\"hypercube:item_instance\":\"18726d32-57ae-4305-b317-2b084283e704\"},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click to open the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"reference menu.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"extra\":[{\"color\":\"gold\",\"text\":\"◆ \"},{\"color\":\"aqua\",\"text\":\"Reference Book \"},{\"color\":\"gold\",\"text\":\"◆\"}],\"text\":\"\"}'}}}")));
                this.handler.slots.get(17).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:blaze_rod\",tag:{CustomModelData:0,HideFlags:127,PublicBukkitValues:{\"hypercube:item_instance\":\"de7f011e-8248-40e6-b04f-8315c4c7be4a\"},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Shows two corresponding brackets\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"for a short amount of time.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-Click a bracket to highlight\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"both.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"color\":\"gold\",\"text\":\"Bracket Finder\"}'}}}")));
                this.handler.slots.get(26).setStack(ItemStack.fromNbt(NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:arrow\",tag:{CustomModelData:0,HideFlags:127,display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Click on a Condition block with this\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to switch between \\'IF\\' and \\'IF NOT\\'.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"color\":\"red\",\"text\":\"NOT Arrow\"}'}}}")));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
//        int i = selectedTab;
//        selectedTab = group.getIndex();
//        this.cursorDragSlots.clear();
//        (this.handler).itemList.clear();
//        this.endTouchDrag();
//        int j;
//        int k;
//        if (group == ItemGroup.HOTBAR) {
//            HotbarStorage hotbarStorage = this.client.getCreativeHotbarStorage();
//
//            for(j = 0; j < 9; ++j) {
//                HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(j);
//                if (hotbarStorageEntry.isEmpty()) {
//                    for(k = 0; k < 9; ++k) {
//                        if (k == j) {
//                            ItemStack itemStack = new ItemStack(Items.PAPER);
//                            itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
//                            Text text = this.client.options.hotbarKeys[j].getBoundKeyLocalizedText();
//                            Text text2 = this.client.options.saveToolbarActivatorKey.getBoundKeyLocalizedText();
//                            itemStack.setCustomName(Text.translatable("inventory.hotbarInfo", new Object[]{text2, text}));
//                            (this.handler).itemList.add(itemStack);
//                        } else {
//                            (this.handler).itemList.add(ItemStack.EMPTY);
//                        }
//                    }
//                } else {
//                    (this.handler).itemList.addAll(hotbarStorageEntry);
//                }
//            }
//        } else if (group != ItemGroup.SEARCH) {
//            group.appendStacks((this.handler).itemList);
//        }
//
//
//        if (this.searchBox != null) {
//            if (group == ItemGroup.SEARCH) {
//                this.searchBox.setVisible(true);
//                this.searchBox.setFocusUnlocked(false);
//                this.searchBox.setTextFieldFocused(true);
//                if (i != group.getIndex()) {
//                    this.searchBox.setText("");
//                }
//
//                this.search();
//            } else {
//                this.searchBox.setVisible(false);
//                this.searchBox.setFocusUnlocked(true);
//                this.searchBox.setTextFieldFocused(false);
//                this.searchBox.setText("");
//            }
//        }
//
//        this.scrollPosition = 0.0F;
//        (this.handler).scrollItems(0.0F);
//    }
//
//    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
//        if (!this.hasScrollbar()) {
//            return false;
//        } else {
//            int i = ((this.handler).itemList.size() + 9 - 1) / 9 - 5;
//            float f = (float)(amount / (double)i);
//            this.scrollPosition = MathHelper.clamp(this.scrollPosition - f, 0.0F, 1.0F);
//            (this.handler).scrollItems(this.scrollPosition);
//            return true;
//        }
//    }
//
//    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
//        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
//        this.lastClickOutsideBounds = bl && !this.isClickInTab(ItemGroup.GROUPS[selectedTab], mouseX, mouseY);
//        return this.lastClickOutsideBounds;
//    }
//
//    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
//        int i = this.x;
//        int j = this.y;
//        int k = i + 175;
//        int l = j + 18;
//        int m = k + 14;
//        int n = l + 112;
//        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
//    }
//
//    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
//        if (this.scrolling) {
//            int i = this.y + 18;
//            int j = i + 112;
//            this.scrollPosition = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
//            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
//            this.handler.scrollItems(this.scrollPosition);
//            return true;
//        } else {
//            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
//        }
//    }
//
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
//
//    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
//        if (selectedTab == ItemGroup.SEARCH.getIndex()) {
//            List<Text> list = stack.getTooltip(this.client.player, this.client.options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL);
//            List<Text> list2 = Lists.newArrayList(list);
//            Item item = stack.getItem();
//            ItemGroup itemGroup = item.getGroup();
//            if (itemGroup == null && stack.isOf(Items.ENCHANTED_BOOK)) {
//                Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
//                if (map.size() == 1) {
//                    Enchantment enchantment = (Enchantment)map.keySet().iterator().next();
//                    ItemGroup[] var11 = ItemGroup.GROUPS;
//                    int var12 = var11.length;
//
//                    for(int var13 = 0; var13 < var12; ++var13) {
//                        ItemGroup itemGroup2 = var11[var13];
//                        if (itemGroup2.containsEnchantments(enchantment.type)) {
//                            itemGroup = itemGroup2;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            this.searchResultTags.forEach((tagKey) -> {
//                if (stack.isIn(tagKey)) {
//                    list2.add(1, Text.literal("#" + tagKey.id()).formatted(Formatting.DARK_PURPLE));
//                }
//
//            });
//            if (itemGroup != null) {
//                list2.add(1, itemGroup.getDisplayName().copy().formatted(Formatting.BLUE));
//            }
//
//            this.renderTooltip(matrices, list2, stack.getTooltipData(), x, y);
//        } else {
//            super.renderTooltip(matrices, stack, x, y);
//        }
//
//    }
//
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DevInventoryGroup itemGroup = DevInventoryGroup.GROUPS[selectedTab];
        DevInventoryGroup[] groups = DevInventoryGroup.GROUPS;


        for(DevInventoryGroup group : groups) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (group.getIndex() != selectedTab && group != CODE_VAULT) {
                this.renderTabIcon(matrices, group);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        String texture = "item_search";
        if(!itemGroup.isHasSearchBar()) texture = "items";
        if(itemGroup == INVENTORY) texture = "inventory";
        RenderSystem.setShaderTexture(0, new Identifier(TAB_TEXTURE_PREFIX + texture + ".png"));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int scrollbarX = this.x + 82;
        int scrollbarY = this.y + 6;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.renderTabIcon(matrices, itemGroup);

        if(itemGroup.isHasSearchBar()) this.searchBox.render(matrices, mouseX, mouseY, delta);
        if (itemGroup != INVENTORY) {
            this.drawTexture(matrices, scrollbarX, scrollbarY + (int)((float)(scrollbarX - scrollbarY - 17) * this.scrollPosition), 232 + 12, 0, 12, 15);
        }
        else {
            InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, (float)(this.x + 88 - mouseX), (float)(this.y + 45 - 30 - mouseY), this.client.player);
        }

    }

    protected boolean isClickInTab(DevInventoryGroup group, double mouseX, double mouseY) {
        int x = group.getDisplayX(0);
        int y = group.getDisplayY(0, this.backgroundHeight);

        return mouseX >= (double)x && mouseX <= (double)(x + TAB_WIDTH)
            && mouseY >= (double)y && mouseY <= (double)(y + TAB_HEIGHT);
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

//        if (group.isSpecial()) {
//            originX = this.x + this.backgroundWidth - 28 * (6 - column);
//        } else if (column > 0) {
//            originX += column;
//        }

//        if (isTopRow) {
//            originY -= TAB_HEIGHT * 2;
//        } else {
////            height += 64;
////            originY += this.backgroundHeight - 4;
//            originY -= TAB_HEIGHT;
//        }

        this.drawTexture(matrices, originX, originY, mapX, mapY, TAB_WIDTH, 32);
        this.itemRenderer.zOffset = 100.0F;
        originX += 6;
        originY += 8 + (isTopRow ? 2 : -2);
        ItemStack itemStack = group.getIcon();
        this.itemRenderer.renderInGuiWithOverrides(itemStack, originX, originY);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, itemStack, originX, originY);
        this.itemRenderer.zOffset = 0.0F;
    }

//    public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
//        ClientPlayerEntity clientPlayerEntity = client.player;
//        HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
//        HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
//    }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }
}
