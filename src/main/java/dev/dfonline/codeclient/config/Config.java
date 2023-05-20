package dev.dfonline.codeclient.config;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.actiondump.ActionDump;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.OptionFlag;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import dev.isxander.yacl.gui.controllers.cycling.EnumController;
import dev.isxander.yacl.gui.controllers.slider.FloatSliderController;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.string.number.IntegerFieldController;
import net.minecraft.text.Text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Config {
    private static Config instance;
    public boolean NoClipEnabled = true;
    public boolean PlaceOnAir = false;
    public int AirSpeed = 10;
    public boolean CodeClientAPI = false;
    public boolean CCDBUG = true;
    public boolean CustomBlockInteractions = true;
    public boolean CustomTagInteraction = false;
    public boolean AutoJoin = false;
    public Node AutoNode = Node.None;
    public boolean AutoJoinPlot = false;
    public int AutoJoinPlotId = 0;
    public CharSetOption FileCharSet = CharSetOption.UTF_8;
    public boolean InvisibleBlocksInDev = false;
    public float ReachDistance = 5;
    public boolean AutoFly = false;
    public LayerInteractionMode CodeLayerInteractionMode = LayerInteractionMode.AUTO;

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
            object.addProperty("AutoJoin", AutoJoin);
            object.addProperty("AutoNode",AutoNode.name());
            object.addProperty("AutoJoinPlot",AutoJoinPlot);
            object.addProperty("AutoJoinPlotId",AutoJoinPlotId);
            object.addProperty("FileCharSet",FileCharSet.name());
            object.addProperty("InvisibleBlocksInDev",InvisibleBlocksInDev);
            object.addProperty("ReachDistance",ReachDistance);
            object.addProperty("AutoFly",AutoFly);
            object.addProperty("CodeLayerInteractionMode",CodeLayerInteractionMode.name());
            FileManager.writeFile("options.json", object.toString());
        } catch (Exception e) {
            CodeClient.LOGGER.info("Couldn't save config: " + e);
        }
    }

    public static Config getConfig() {
        if(instance == null) {
            try {
                instance = CodeClient.gson.fromJson(FileManager.readFile("options.json", StandardCharsets.UTF_8), Config.class);
            }
            catch (Exception exception) {
                CodeClient.LOGGER.info("Config didn't load: " + exception);
                instance = new Config();
            }
        }
        return instance;
    }
    public static void clear() {
        instance = null;
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
                                .available(false)
                                .build())
                        .option(Option.createBuilder(CharSetOption.class)
                                .name(Text.literal("File Charset"))
                                .tooltip(Text.literal("There can often be artifacts in the ActionDump, change this to fix it."))
                                .binding(
                                        CharSetOption.UTF_8,
                                        () -> FileCharSet,
                                        opt -> FileCharSet = opt
                                )
                                .flag(minecraftClient -> ActionDump.clear())
                                .controller(EnumController::new)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Show Invisible Blocks"))
                                .tooltip(Text.literal("If invisible blocks like barriers should be shown whilst in dev and build."))
                                .binding(
                                        false,
                                        () -> InvisibleBlocksInDev,
                                        opt -> InvisibleBlocksInDev = opt
                                )
                                .controller(TickBoxController::new)
                                .available(false)
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.literal("Reach Distance"))
                                .tooltip(Text.literal("Modified reach distance in dev mode.\nThis will teleport you closer if something is too far away."))
                                .binding(
                                        5f,
                                        () -> ReachDistance,
                                        opt -> ReachDistance = opt
                                )
                                .controller(floatOption -> new FloatSliderController(floatOption,5,10,0.1f))
                                .available(false)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto Fly"))
                                .tooltip(Text.literal("Automatically runs /fly when you go to spawn."))
                                .binding(
                                        false,
                                        () -> AutoFly,
                                        opt -> AutoFly = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(LayerInteractionMode.class)
                                .name(Text.literal("Layer Interaction"))
                                .tooltip(layerInteractionMode -> Text.literal("How you interact with layers and virtual layers.\n" + layerInteractionMode.description))
                                .binding(
                                        LayerInteractionMode.AUTO,
                                        () -> CodeLayerInteractionMode,
                                        opt -> CodeLayerInteractionMode = opt
                                )
                                .controller(EnumController::new)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("AutoJoin"))
                        .tooltip(Text.literal("If and where to auto join"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Enabled"))
                                .tooltip(Text.literal("If CodeClient should automatically connect you to DF."))
                                .binding(
                                        false,
                                        () -> AutoJoin,
                                        opt -> AutoJoin = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(Node.class)
                                .name(Text.literal("Node"))
                                .tooltip(Text.literal("Which node you should be sent to, or have DF handle it."))
                                .binding(
                                        Node.None,
                                        () -> AutoNode,
                                        opt -> AutoNode = opt
                                )
                                .controller(EnumController::new)
//                                .available(AutoJoin)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto join plot"))
                                .tooltip(Text.literal("If you should automatically be sent to a plot."))
                                .binding(
                                        false,
                                        () -> AutoJoinPlot,
                                        opt -> AutoJoinPlot = opt
                                )
                                .controller(TickBoxController::new)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.literal("Plot ID"))
                                .tooltip(Text.literal("The plot ID of the plot to automatically join, if enabled."))
                                .binding(
                                        0,
                                        () -> AutoJoinPlotId,
                                        opt -> AutoJoinPlotId = opt
                                )
                                .controller(IntegerFieldController::new)
                                .build())
                        .build())
                .save(this::save)
                .build();
    }

    public enum Node {
        None(""),
        Node1("node1."),
        Node2("node2."),
        Node3("node3."),
        Node4("node4."),
        Node5("node5."),
        Node6("node6."),
        Node7("node7."),
        Beta("beta.");

        public final String prepend;
        Node(String prepend) {
            this.prepend = prepend;
        }
    }

    public enum CharSetOption {
        ISO_8859_1(StandardCharsets.ISO_8859_1),
        UTF_8(StandardCharsets.UTF_8);

        public final Charset charSet;
        CharSetOption(Charset charSet) {
            this.charSet = charSet;
        }
    }

    public enum LayerInteractionMode {
        OFF("Makes interaction with layers default."),
        AUTO("When you are above a layer it is a virtual barrier layer."),
        ON("There are always virtual barrier layers.");

        public final String description;
        LayerInteractionMode(String description) {
            this.description = description;
        }
    }

    public Config() {
    }
}
