package dev.dfonline.codeclient.dev.menu.devinventory;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.actiondump.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DevInventoryGroup {

    public static final int TAB_WIDTH = 28;
    public static final int TAB_HEIGHT = 28;
    public static final DevInventoryGroup[] GROUPS = new DevInventoryGroup[21];
    public static final DevInventoryGroup PLAYER_EVENT = new DevInventoryGroup("event", "Player Event", Items.DIAMOND_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup PLAYER_IF = new DevInventoryGroup("if_player", "If Player", Items.OAK_PLANKS.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup PLAYER_ACTION = new DevInventoryGroup("player_action", "Player Action", Items.COBBLESTONE.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_EVENT = new DevInventoryGroup("entity_event", "Entity Event", Items.GOLD_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_IF = new DevInventoryGroup("if_entity", "If Entity", Items.BRICKS.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_ACTION = new DevInventoryGroup("entity_action", "Entity Action", Items.MOSSY_COBBLESTONE.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup VAR_SET = new DevInventoryGroup("set_var", "Set Variable", Items.IRON_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup VAR_IF = new DevInventoryGroup("if_var", "If Variable", Items.OBSIDIAN.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup GAME_EVENT = new DevInventoryGroup("game_event", "Game Event", Items.NETHERITE_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup GAME_IF = new DevInventoryGroup("if_game", "If Game", Items.RED_NETHER_BRICKS.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup GAME_ACTION = new DevInventoryGroup("game_action", "Game Action", Items.NETHERRACK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup CONTROL = new DevInventoryGroup("control", "Control", Items.COAL_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup SELECT_OBJECT = new DevInventoryGroup("select_obj", "Select Object", Items.PURPUR_BLOCK.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup REPEAT = new DevInventoryGroup("repeat", "Repeat", Items.PRISMARINE.getDefaultInstance(), true).useCodeBlock();
    public static final DevInventoryGroup SOUNDS = new DevInventoryGroup("sound", "Sounds", Items.NAUTILUS_SHELL.getDefaultInstance(), false);
    public static final DevInventoryGroup PARTICLES = new DevInventoryGroup("particle", "Particles", Items.WHITE_DYE.getDefaultInstance(), false);
    public static final DevInventoryGroup POTIONS = new DevInventoryGroup("potion", "Potions", Items.DRAGON_BREATH.getDefaultInstance(), false);
    public static final DevInventoryGroup GAME_VALUES = new DevInventoryGroup("game_value", "Game Values", Items.NAME_TAG.getDefaultInstance(), false);
    public static final DevInventoryGroup SEARCH = new DevInventoryGroup("search", "Search All", Items.COMPASS.getDefaultInstance(), false).useCodeBlock();
    public static final DevInventoryGroup SAVED_TEMPLATES = new DevInventoryGroup("saved_templates", "Saved Templates", Items.ENDER_CHEST.getDefaultInstance(), false);
    public static final DevInventoryGroup INVENTORY = new DevInventoryGroup("inventory", "Inventory", Items.CHEST.getDefaultInstance(), false).disableSearch();
    public static int TOP_HALF = -1;
    private static int global = 0;

    static {
        try {
            ActionDump actionDump = ActionDump.getActionDump();
            GAME_VALUES.useCategory(actionDump.gameValues);
            SOUNDS.useCategory(actionDump.sounds);
            POTIONS.useCategory(actionDump.potions);
            PARTICLES.useCategory(actionDump.particles);

            SAVED_TEMPLATES.setItemsProvider(query -> {
                ArrayList<Searchable> items = new ArrayList<>();
                if (query == null) query = "";
                query = query.toLowerCase();
                for (Searchable template : readTemplates(FileManager.templatesPath())) {
                    for (String term : template.getTerms())
                        if (term.toLowerCase().contains(query)) {
                            items.add(template);
                            break;
                        }
                }
                return items;
            });
            SEARCH.setItemsProvider(query -> {
                ArrayList<Searchable> items = new ArrayList<>();
                if (query == null) query = "";
                else query = query.toLowerCase();
                for (Action action : actionDump.actions) {
                    if (action.isInvalid()) continue;
                    for (String term : action.getTerms())
                        if (term.toLowerCase().contains(query)) {
                            items.add(action);
                            break;
                        }
                }
                for (GameValue gameValue : actionDump.gameValues) {
                    for (String term : gameValue.getTerms())
                        if (term.toLowerCase().contains(query)) {
                            items.add(gameValue);
                            break;
                        }
                }
                for (Sound sound : actionDump.sounds) {
                    for (String term : sound.getTerms())
                        if (term.toLowerCase().contains(query)) {
                            items.add(sound);
                            break;
                        }
                }
                for (Potion potion : actionDump.potions) {
                    for (String term : potion.getTerms())
                        if (term.toLowerCase().contains(query)) {
                            items.add(potion);
                            break;
                        }
                }
                return items;
            });
        } catch (Exception ignored) {
        }
    }

    private final int index;
    private final String id;
    private final ItemStack icon;
    private final Component name;
    private final boolean topHalf;
    private boolean hasSearchBar = true;
    private ItemsProvider itemsProvider = null;
    public DevInventoryGroup(String id, String name, ItemStack icon, boolean topHalf) {
        this.index = global;
        this.id = id;
        this.icon = icon;
        this.name = Component.nullToEmpty(name);
        this.topHalf = topHalf;
        if (!topHalf && TOP_HALF == -1) TOP_HALF = global + 1;


        GROUPS[index] = this;
        global++;
    }

    private static ArrayList<Searchable> emptyMenu() {
        ArrayList<Searchable> items = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            items.add(new Searchable.StaticSearchable(Items.AIR.getDefaultInstance()));
        }
        return items;
    }

    static private ArrayList<Searchable> readTemplates(Path path) {
        return readTemplates(path, path);
    }

    static private ArrayList<Searchable> readTemplates(Path path, Path root) {
        var list = new ArrayList<Searchable>();
        try {
            var dir = Files.list(path);
            for (var file : dir.toList()) {
                if (Files.isDirectory(file)) list.addAll(readTemplates(file, root));
                if (file.getFileName().toString().endsWith(".dft")) {
                    byte[] data = Files.readAllBytes(file);
                    ItemStack template = Utility.makeTemplate(new String(Base64.getEncoder().encode(data)));
                    DFItem item = DFItem.of(template);
                    item.setName(Component.empty().withStyle(ChatFormatting.RED).append("Saved Template").append(Component.literal(" » ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)).append(String.valueOf(root.relativize(file))));
                    list.add(new Searchable.StaticSearchable(item.getItemStack()));
                }
            }
            dir.close();
        } catch (IOException ignored) {
        }
        return list;
    }

    private DevInventoryGroup disableSearch() {
        this.itemsProvider = null;
        this.hasSearchBar = false;
        return this;
    }

    private DevInventoryGroup setItemsProvider(ItemsProvider itemsProvider) {
        this.itemsProvider = itemsProvider;
        return this;
    }

    public List<Searchable> searchItems(String query) {
        if (this.itemsProvider == null) return null;
        return this.itemsProvider.run(query);
    }

    public Component getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean hasSearchBar() {
        return hasSearchBar;
    }

    public boolean isTopHalf() {
        return this.topHalf;
    }

    public int getPlace() {
        return this.index;
    }

    public int getColumn() {
        return getPlace() % 7;
    }

    public int getRow() {
        return getPlace() / 7;
    }

    public int getDisplayX(int originX) {
        return originX + getColumn() * TAB_WIDTH;
    }

    public int getDisplayY(int originY, int menuHeight) {
        int multiplier = TAB_HEIGHT * (isTopHalf() ? -1 : 1);
        int y = originY + (isTopHalf() ? (TAB_HEIGHT * -1 - 4) + (getRow() * multiplier) : menuHeight);
        return y;
    }

    private DevInventoryGroup useCodeBlock() {
        this.itemsProvider = query -> {
            ArrayList<Searchable> items = new ArrayList<>();
            try {
                if (query == null) for (CodeBlock codeblock : ActionDump.getActionDump().codeblocks) {
                    if (codeblock.identifier.equals(this.id)) items.add(codeblock);
                }
                for (Action action : ActionDump.getActionDump().actions) {
                    if (action.getCodeBlock().identifier.equals(this.id) && !action.isInvalid()) {
                        if (query == null) items.add(action);
                        else {
                            for (String term : action.getTerms()) {
                                if (term.toLowerCase().contains(query.toLowerCase())) items.add(action);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return items;
        };
        return this;
    }

    private DevInventoryGroup useCategory(Searchable[] categoryValues) {
        this.itemsProvider = query -> {
            ArrayList<Searchable> items = new ArrayList<>();
            for (Searchable value : categoryValues) {
                if (query == null) items.add(value);
                else {
                    for (String term : value.getTerms()) {
                        if (term.toLowerCase().replaceAll("[_ ]", "").contains(query.toLowerCase())) {
                            items.add(value);
                            break;
                        }
                    }
                }
            }
            return items;
        };
        return this;
    }

    interface ItemsProvider {
        List<Searchable> run(String query);
    }
}
