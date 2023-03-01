package dev.dfonline.codeclient.dev.DevInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class DevInventoryGroup {

    private final int index;
    private final String id;
    private final ItemStack icon;
    private final Text name;
    private final boolean topHalf;
    private boolean hasSearchBar = true;

    private static int global = 0;
    public DevInventoryGroup(String id, String name, ItemStack icon, boolean topHalf) {
        this.index = global;
        this.id = id;
        this.icon = icon;
        this.name = Text.of(name);
        this.topHalf = topHalf;
        if(!topHalf && TOP_HALF == -1) TOP_HALF = global + 1;


        GROUPS[index] = this;
        global++;
    }
    private DevInventoryGroup disableSearch() {
        this.hasSearchBar = false;
        return this;
    }

    public Text getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean isHasSearchBar() {
        return hasSearchBar;
    }

    public boolean isTopHalf() {
        return this.topHalf;
    }
    public int getPlace() {
        return this.index - (isTopHalf() ? 0 : TOP_HALF - 1);
    }

    public int getColumn() {
        return getPlace() % 7;
    }
    public int getRow() { return getPlace() / 7; }



    public int getDisplayX(int originX) {
        return originX + getColumn() * TAB_WIDTH;
    }
    public int getDisplayY(int originY, int menuHeight) {
        int multiplier = TAB_HEIGHT * (isTopHalf() ? -1 : 1);
        int y = (getRow() * multiplier) + originY + (isTopHalf() ? (TAB_HEIGHT * -1 - 4) : menuHeight);
        return y;
    }

    public static final int TAB_WIDTH = 28;
    public static final int TAB_HEIGHT = 28;
    

    public static int TOP_HALF = -1;
    public static final DevInventoryGroup[] GROUPS = new DevInventoryGroup[21];

    public static final DevInventoryGroup PLAYER_EVENT = new DevInventoryGroup("event", "Player Event", Items.DIAMOND_BLOCK.getDefaultStack(), true);
    public static final DevInventoryGroup PLAYER_IF = new DevInventoryGroup("if_player", "If Player", Items.OAK_PLANKS.getDefaultStack(), true);
    public static final DevInventoryGroup PLAYER_ACTION = new DevInventoryGroup("player_action", "Player Action", Items.COBBLESTONE.getDefaultStack(), true);
    public static final DevInventoryGroup ENTITY_EVENT = new DevInventoryGroup("entity_event", "Entity Event", Items.GOLD_BLOCK.getDefaultStack(), true);
    public static final DevInventoryGroup ENTITY_IF = new DevInventoryGroup("if_entity", "If Entity", Items.BRICKS.getDefaultStack(), true);
    public static final DevInventoryGroup ENTITY_ACTION = new DevInventoryGroup("game_action", "Entity Action", Items.MOSSY_COBBLESTONE.getDefaultStack(), true);
    public static final DevInventoryGroup SEARCH = new DevInventoryGroup("search", "Search All", Items.COMPASS.getDefaultStack(), true);
    public static final DevInventoryGroup VAR_SET = new DevInventoryGroup("set_var", "Set Variable", Items.IRON_BLOCK.getDefaultStack(), true);
    public static final DevInventoryGroup VAR_IF = new DevInventoryGroup("if_var", "If Variable", Items.OBSIDIAN.getDefaultStack(), true);
    public static final DevInventoryGroup GAME_ACTION = new DevInventoryGroup("game_action", "Game Action", Items.NETHERRACK.getDefaultStack(), true);
    public static final DevInventoryGroup GAME_IF = new DevInventoryGroup("if_game", "If Game", Items.RED_NETHER_BRICKS.getDefaultStack(), true);
    public static final DevInventoryGroup CONTROL = new DevInventoryGroup("control", "Control", Items.COAL_BLOCK.getDefaultStack(), true);
    public static final DevInventoryGroup SELECT_OBJECT = new DevInventoryGroup("select_obj", "Select Object", Items.PURPUR_BLOCK.getDefaultStack(), true);
    public static final DevInventoryGroup REPEAT = new DevInventoryGroup("repeat", "Repeat", Items.PRISMARINE.getDefaultStack(), true);
    public static final DevInventoryGroup SOUNDS = new DevInventoryGroup("sound", "Sounds", Items.NAUTILUS_SHELL.getDefaultStack(), false);
    public static final DevInventoryGroup PARTICLES = new DevInventoryGroup("particle", "Particles", Items.WHITE_DYE.getDefaultStack(), false);
    public static final DevInventoryGroup POTIONS = new DevInventoryGroup("potion", "Potions", Items.DRAGON_BREATH.getDefaultStack(), false);
    public static final DevInventoryGroup GAME_VALUES = new DevInventoryGroup("game_value", "Game Values", Items.NAME_TAG.getDefaultStack(), false);
    public static final DevInventoryGroup OTHERS = new DevInventoryGroup("others", "Other Items", Items.IRON_INGOT.getDefaultStack(), false).disableSearch();
    public static final DevInventoryGroup CODE_VAULT = new DevInventoryGroup("code_vault", "Code Vault", Items.ENDER_CHEST.getDefaultStack(), false);
    public static final DevInventoryGroup INVENTORY = new DevInventoryGroup("inventory", "Inventory", Items.CHEST.getDefaultStack(), false).disableSearch();
}
