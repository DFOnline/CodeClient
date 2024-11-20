package dev.dfonline.codeclient.dev.menu.devinventory;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.actiondump.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    public static final DevInventoryGroup PLAYER_EVENT = new DevInventoryGroup("event", "Player Event", Items.DIAMOND_BLOCK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup PLAYER_IF = new DevInventoryGroup("if_player", "If Player", Items.OAK_PLANKS.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup PLAYER_ACTION = new DevInventoryGroup("player_action", "Player Action", Items.COBBLESTONE.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_EVENT = new DevInventoryGroup("entity_event", "Entity Event", Items.GOLD_BLOCK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_IF = new DevInventoryGroup("if_entity", "If Entity", Items.BRICKS.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup ENTITY_ACTION = new DevInventoryGroup("entity_action", "Entity Action", Items.MOSSY_COBBLESTONE.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup SEARCH = new DevInventoryGroup("search", "Search All", Items.COMPASS.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup VAR_SET = new DevInventoryGroup("set_var", "Set Variable", Items.IRON_BLOCK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup VAR_IF = new DevInventoryGroup("if_var", "If Variable", Items.OBSIDIAN.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup GAME_ACTION = new DevInventoryGroup("game_action", "Game Action", Items.NETHERRACK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup GAME_IF = new DevInventoryGroup("if_game", "If Game", Items.RED_NETHER_BRICKS.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup CONTROL = new DevInventoryGroup("control", "Control", Items.COAL_BLOCK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup SELECT_OBJECT = new DevInventoryGroup("select_obj", "Select Object", Items.PURPUR_BLOCK.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup REPEAT = new DevInventoryGroup("repeat", "Repeat", Items.PRISMARINE.getDefaultStack(), true).useCodeBlock();
    public static final DevInventoryGroup SOUNDS = new DevInventoryGroup("sound", "Sounds", Items.NAUTILUS_SHELL.getDefaultStack(), false);
    public static final DevInventoryGroup PARTICLES = new DevInventoryGroup("particle", "Particles", Items.WHITE_DYE.getDefaultStack(), false);
    public static final DevInventoryGroup POTIONS = new DevInventoryGroup("potion", "Potions", Items.DRAGON_BREATH.getDefaultStack(), false);
    public static final DevInventoryGroup GAME_VALUES = new DevInventoryGroup("game_value", "Game Values", Items.NAME_TAG.getDefaultStack(), false);
    public static final DevInventoryGroup OTHERS = new DevInventoryGroup("others", "Other Items", Items.IRON_INGOT.getDefaultStack(), false).disableSearch();
    public static final DevInventoryGroup SAVED_TEMPLATES = new DevInventoryGroup("saved_templates", "Saved Templates", Items.ENDER_CHEST.getDefaultStack(), false);
    public static final DevInventoryGroup INVENTORY = new DevInventoryGroup("inventory", "Inventory", Items.CHEST.getDefaultStack(), false).disableSearch();
    public static int TOP_HALF = -1;
    private static int global = 0;

    static {
        OTHERS.setItemsProvider(query -> {
            ArrayList<Searchable> items = emptyMenu();
            try {
                items.set(0, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"txt\",\"data\":{\"name\":\"string\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"aqua\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"String\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"A series of characters which\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"is highly manipulatable.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Recommended for variable\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"operations.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Type in the chat while\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"holding this item.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"E.g. \\'\",\"underlined\":false},{\"color\":\"white\",\"italic\":false,\"text\":\"Sample text\"},{\"color\":\"gray\",\"italic\":false,\"text\":\"\\'\"}],\"text\":\"\"}']},count:1,id:\"minecraft:string\"}")).get()));
                items.set(1, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"comp\",\"data\":{\"name\":\"text\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#7FD42A\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Styled Text\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Text with extra formatting via\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"color\":\"white\",\"italic\":false,\"text\":\"MiniMessage\"},{\"color\":\"gray\",\"italic\":false,\"text\":\" tags such as \"},{\"color\":\"white\",\"italic\":false,\"text\":\"<color>\"},{\"color\":\"gray\",\"italic\":false,\"text\":\".\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Recommended for text displayed\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"through chat, item names, and others.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Type in the chat while\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"holding this item.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"E.g. \\'\",\"underlined\":false},{\"color\":\"white\",\"italic\":false,\"text\":\"<rainbow>Rainbow text!\"},{\"color\":\"gray\",\"italic\":false,\"text\":\"\\'\"}],\"text\":\"\"}']},count:1,id:\"minecraft:book\"}")).get()));
                items.set(2, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"num\",\"data\":{\"name\":\"0\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"red\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Number\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Represents a number of an\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"integer or a decimal. It can\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"have up to 3 decimal places.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Type a number in chat while\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"holding this item.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"E.g. \",\"underlined\":false},{\"color\":\"white\",\"italic\":false,\"text\":\"4\"},{\"color\":\"gray\",\"italic\":false,\"text\":\" or \"},{\"color\":\"white\",\"italic\":false,\"text\":\"1.25\"}],\"text\":\"\"}']},count:1,id:\"minecraft:slime_ball\"}")).get()));
                items.set(3, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"loc\",\"data\":{\"isBlock\":false,\"loc\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"pitch\":0.0,\"yaw\":0.0}}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"green\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Location\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Represents a location in the plot.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"[0,0,0] points to the north-west\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"bottom corner of the plot.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click this item to set it\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"to your current location.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Left-click to set it to a block.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Shift left-click to teleport to it.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:paper\"}")).get()));
                items.set(4, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"vec\",\"data\":{\"x\":0.0,\"y\":0.0,\"z\":0.0}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#2AFFAA\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Vector\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"A vector consists of X, Y and\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Z values. Used for multiple\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"purposes such as representing\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"a direction, motion or an offset.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Use the /vector command or\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"type \",\"underlined\":false},{\"color\":\"aqua\",\"italic\":false,\"text\":\"\\\\\"<x> <y> <z>\\\\\"\"},{\"color\":\"gray\",\"italic\":false,\"text\":\" to set the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"components. Left-click to\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"preview it.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:prismarine_shard\"}")).get()));
                items.set(5, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"snd\",\"data\":{\"pitch\":1.0,\"vol\":2.0,\"sound\":\"Pling\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"blue\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Sound\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Represents a Minecraft sound or a\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"custom sound from resource pack.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click this item to select\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"a sound effect.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Shift right-click to select\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"a variant of the sound.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"To add a pitch & volume, type\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"them in one chat line, in order.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Left-click to preview it.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:nautilus_shell\"}")).get()));
                items.set(6, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"part\",\"data\":{\"particle\":\"Cloud\",\"cluster\":{\"amount\":1,\"horizontal\":0.0,\"vertical\":0.0},\"data\":{\"x\":1.0,\"y\":0.0,\"z\":0.0,\"motionVariation\":100}}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#AA55FF\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Particle\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Represents a particle effect with\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"customizable parameters.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click this item to select\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"a particle effect.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Use the /particle command\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"to change its parameters.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Left-click to preview it.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:white_dye\"}")).get()));

                items.set(9, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"pot\",\"data\":{\"pot\":\"Speed\",\"dur\":1000000,\"amp\":0}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#FF557F\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Potion Effect\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Represents a potion effect with\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"custom amplifier and duration.\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click this item to select\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"a potion effect.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Use the /potion command or\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"type \",\"underlined\":false},{\"color\":\"aqua\",\"italic\":false,\"text\":\"\\\\\"<amplifier> [duration]\\\\\"\"},{\"color\":\"gray\",\"italic\":false,\"text\":\"\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"to set its options. Left-click\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"to preview it.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:dragon_breath\"}")).get()));
                items.set(10, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"var\",\"data\":{\"name\":\"§eVariable\",\"scope\":\"unsaved\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"yellow\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Variable\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Variables can store values inside.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Give a name to your variable, and\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"use \",\"underlined\":false},{\"color\":\"white\",\"italic\":false,\"text\":\"Set Variable\"},{\"color\":\"gray\",\"italic\":false,\"text\":\" to set its value.\"}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Type a variable name in chat\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"while holding this item.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Shift right-click for options.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:magma_cream\"}")).get()));
                items.set(11, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"g_val\",\"data\":{\"type\":\"Location\",\"target\":\"Default\"}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#FFD47F\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Game Value\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"An automatically set value based\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"on the game\\'s current conditions\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"(e.g. a player\\'s location).\",\"underlined\":false}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click to select a game\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"value and its target.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:name_tag\"}")).get()));
                items.set(12, new Searchable.StaticSearchable(ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{components:{\"minecraft:attribute_modifiers\":{modifiers:[],show_in_tooltip:0b},\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:varitem\":'{\"id\":\"pn_el\",\"data\":{\"name\":\"name\",\"type\":\"any\",\"plural\":false,\"optional\":false}}'}},\"minecraft:custom_model_data\":5000,\"minecraft:custom_name\":'{\"extra\":[{\"bold\":false,\"color\":\"#AAFFAA\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Parameter\",\"underlined\":false}],\"text\":\"\"}',\"minecraft:hide_additional_tooltip\":{},\"minecraft:lore\":['{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"A parameter to put in the\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"code chest of a \",\"underlined\":false},{\"color\":\"aqua\",\"italic\":false,\"text\":\"function\"},{\"color\":\"gray\",\"italic\":false,\"text\":\".\"}],\"text\":\"\"}','\"\"','{\"extra\":[{\"bold\":false,\"color\":\"light_purple\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"How to set:\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Type in chat to edit name.\",\"underlined\":false}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"color\":\"gray\",\"italic\":false,\"obfuscated\":false,\"strikethrough\":false,\"text\":\"Right-click to open the editor.\",\"underlined\":false}],\"text\":\"\"}']},count:1,id:\"minecraft:ender_eye\"}")).get()));


                int i = 0;
                for (CodeBlock codeblock : ActionDump.getActionDump().codeblocks) {
                    items.set(27 + i, codeblock);
                    i++;
                }

                //  Items bellow except arrow cannot be normally given since they have an item_instance that changes per node and every restart,
                //  so unless the player is in dev mode with these items in their inventory already we cannot display the usable items.
                //  Caching the item_instance for each node can also be a solution.
//                items.set(8,ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:written_book\",tag:{CustomModelData:0,HideFlags:127,PublicBukkitValues:{\"hypercube:item_instance\":\"18726d32-57ae-4305-b317-2b084283e704\"},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-click to open the\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"reference menu.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"extra\":[{\"color\":\"gold\",\"text\":\"◆ \"},{\"color\":\"aqua\",\"text\":\"Reference Book \"},{\"color\":\"gold\",\"text\":\"◆\"}],\"text\":\"\"}'}}}")));
//                items.set(17,ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:blaze_rod\",tag:{CustomModelData:0,HideFlags:127,PublicBukkitValues:{\"hypercube:item_instance\":\"de7f011e-8248-40e6-b04f-8315c4c7be4a\"},display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Shows two corresponding brackets\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"for a short amount of time.\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Right-Click a bracket to highlight\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"both.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"color\":\"gold\",\"text\":\"Bracket Finder\"}'}}}")));
//                items.set(26, ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), NbtHelper.fromNbtProviderString("{Count:1b,id:\"minecraft:arrow\",tag:{CustomModelData:0,HideFlags:127,display:{Lore:['{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"Click on a Condition block with this\"}],\"text\":\"\"}','{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"gray\",\"text\":\"to switch between \\'IF\\' and \\'IF NOT\\'.\"}],\"text\":\"\"}'],Name:'{\"italic\":false,\"color\":\"red\",\"text\":\"NOT Arrow\"}'}}}")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        });
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
    private final Text name;
    private final boolean topHalf;
    private boolean hasSearchBar = true;
    private ItemsProvider itemsProvider = null;
    public DevInventoryGroup(String id, String name, ItemStack icon, boolean topHalf) {
        this.index = global;
        this.id = id;
        this.icon = icon;
        this.name = Text.of(name);
        this.topHalf = topHalf;
        if (!topHalf && TOP_HALF == -1) TOP_HALF = global + 1;


        GROUPS[index] = this;
        global++;
    }

    private static ArrayList<Searchable> emptyMenu() {
        ArrayList<Searchable> items = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            items.add(new Searchable.StaticSearchable(Items.AIR.getDefaultStack()));
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
                    item.setName(Text.empty().formatted(Formatting.RED).append("Saved Template").append(Text.literal(" » ").formatted(Formatting.DARK_RED, Formatting.BOLD)).append(String.valueOf(root.relativize(file))));
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

    public Text getName() {
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
