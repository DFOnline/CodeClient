package dev.dfonline.codeclient.dev.menu.customchest;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import net.minecraft.util.Identifier;

public class CustomChestNumbers {
    public static final CustomChestNumbers SMALL = new CustomChestNumbers(
            CodeClient.getId("textures/gui/container/custom_chest/background.png"),
            176,
            216,
            202,
            229,
            6,
            6,
            128,
            25,
            12,
            15,
            156,
            14,
            106,
            7,
            13,
            7,
            133,
            -1);
    public static final CustomChestNumbers LARGE = new CustomChestNumbers(
            CodeClient.getId("textures/gui/container/custom_chest/background_big.png"),
            198,
            294,
            228,
            294,
            15,
            10,
            156,
            26,
            6,
            27,
            184,
            18,
            178,
            7,
            17,
            29,
            211,
            210);
    public final Identifier TEXTURE;
    public final int MENU_WIDTH;
    public final int MENU_HEIGHT;
    public final int TEXTURE_WIDTH;
    public final int TEXTURE_HEIGHT;
    public final int SLOTS;
    public final int WIDGETS;
    public final int WIDGET_WIDTH;
    public final int WIDGET_X;
    public final int SCROLL_WIDTH;
    public final int SCROLL_HEIGHT;
    /**
     * Not UV
     */
    public final int SCROLL_POS_X;
    /**
     * Not UV
     */
    public final int SCROLL_POS_Y;
    /**
     * How tall the area the scrollbar is, including the end where the scrollbar hits the end.
     */
    public final int SCROLL_ROOM;
    public final int SLOT_X;
    /**
     * The Y of the first slot and widget. Each stack underneath this.
     */
    public final int SLOT_Y;
    public final int INVENTORY_X;
    public final int INVENTORY_Y;
    public final int DISABLED_X;

    private CustomChestNumbers(Identifier texture, int menuWidth, int menuHeight, int textureWidth, int textureHeight, int slots, int widgets, int widgetWidth, int widgetX, int scrollWidth, int scrollHeight, int scrollPosX, int scrollPosY, int scrollRoom, int slotX, int slotY, int inventoryX, int inventoryY, int disabledX) {
        TEXTURE = texture;
        MENU_WIDTH = menuWidth;
        MENU_HEIGHT = menuHeight;
        TEXTURE_WIDTH = textureWidth;
        TEXTURE_HEIGHT = textureHeight;
        SLOTS = slots;
        WIDGETS = widgets;
        WIDGET_WIDTH = widgetWidth;
        WIDGET_X = widgetX;
        SCROLL_WIDTH = scrollWidth;
        SCROLL_HEIGHT = scrollHeight;
        SCROLL_POS_X = scrollPosX;
        SCROLL_POS_Y = scrollPosY;
        SCROLL_ROOM = scrollRoom;
        SLOT_X = slotX;
        SLOT_Y = slotY;
        INVENTORY_X = inventoryX;
        INVENTORY_Y = inventoryY;
        DISABLED_X = disabledX;
    }

    public static CustomChestNumbers getSize() {
        return Config.getConfig().CustomCodeChest.size;
    }
}
