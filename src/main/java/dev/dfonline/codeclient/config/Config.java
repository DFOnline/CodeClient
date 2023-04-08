package dev.dfonline.codeclient.config;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.location.Plot;
import dev.isxander.yacl.api.*;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.string.StringController;
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.text.Text;

public class Config {
    private static Config instance;
    public boolean NoClipEnabled = true;
    public boolean PlaceOnAir = false;
    public int AirSpeed = 10;
    public boolean CodeClientAPI = false;
    public boolean CCDBUG = true;
    public boolean CustomBlockInteractions = true;
    public boolean CustomTagInteraction = false;

    private void save() {
        try {
            JsonObject object = new JsonObject();
            object.addProperty("NoClipEnabled",NoClipEnabled);
            object.addProperty("AirSpeed", AirSpeed);
            object.addProperty("PlaceOnAir",PlaceOnAir);
            object.addProperty("CodeClientAPI",CodeClientAPI);
            object.addProperty("CCDBUG",CCDBUG);
            object.addProperty("CustomBlockInteractions",CustomBlockInteractions);
            object.addProperty("CustomTagInteraction",CustomTagInteraction);
            FileManager.writeFile("options.json", object.toString());
        } catch (Exception e) {
            CodeClient.LOGGER.info("Couldn't save config: " + e);
        }
    }

    public static Config getConfig() {
        if(instance == null) {
            try {
                instance = CodeClient.gson.fromJson(FileManager.readFile("options.json"), Config.class);
            }
            catch (Exception exception) {
                CodeClient.LOGGER.info("Config didn't load: " + exception);
                instance = new Config();
            }
        }
        return instance;
    }

    public YetAnotherConfigLib getLibConfig() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("CodeClient Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("CodeClient"))
                        .tooltip(Text.literal("Enable or disable features of CodeClient"))
//                        .option(Option.createBuilder(boolean.class)
//                                .name(Text.literal("NoClip"))
//                                .tooltip(Text.literal("If you can NoClip in the dev space."))
//                                .binding(
//                                        true,
//                                        () -> NoClipEnabled,
//                                        option -> NoClipEnabled = option
//                                )
//                                .controller(TickBoxController::new)
//                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.literal("AirStrafe Modifier"))
                                .tooltip(Text.literal("How much faster you go when jumping in dev space."))
                                .binding(
                                        10,
                                        () -> AirSpeed,
                                        opt -> AirSpeed = opt
                                )
                                .controller(opt -> new IntegerSliderController(opt, 10, 30, 1))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Place on Air"))
                                .tooltip(Text.literal("Allows you to place on the virtual barriers in the dev space."))
                                .binding(
                                        false,
                                        () -> PlaceOnAir,
                                        opt -> PlaceOnAir = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("CodeClient API"))
                                .tooltip(Text.literal("Enables/disabled the CC API, which allows authenticated apps to place templates and clear plots.\n(requires restart)"))
                                .binding(
                                        false,
                                        () -> CodeClientAPI,
                                        opt -> CodeClientAPI = opt
                                )
                                .controller(TickBoxController::new)
                                .flag(OptionFlag.GAME_RESTART)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("CCDBUG"))
                                .tooltip(Text.literal("Toggle CCDBUG, which is a variable and entity watcher for debugging."))
                                .binding(
                                        true,
                                        () -> CCDBUG,
                                        opt -> CCDBUG = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Custom Block Interactions"))
                                .tooltip(Text.literal("Toggle weather codespace blocks are instantly hidden and no flashing placed block."))
                                .binding(
                                        true,
                                        () -> CustomBlockInteractions,
                                        opt -> CustomBlockInteractions = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Custom Tag Interaction"))
                                .tooltip(Text.literal("Toggle \"faster\" block tags, with interactions client handled."))
                                .binding(
                                        false,
                                        () -> CustomTagInteraction,
                                        opt -> CustomTagInteraction = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .build())
                .save(this::save)
                .build();
    }

    public enum Node {
        None,
        Node1,
        Node2,
        Node3,
        Node4,
        Node5,
        Node6,
        Node7,
        Beta,
    }

    public Config() {
    }
}
