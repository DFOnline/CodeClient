package dev.dfonline.codeclient.config;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    public boolean AirControl = false;
    public boolean FocusSearch = false;
    public CharSetOption SaveCharSet = CharSetOption.UTF_8;

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
            object.addProperty("AirControl",AirControl);
            object.addProperty("FocusSearch",FocusSearch);
            object.addProperty("SaveCharSet",SaveCharSet.name());
            FileManager.writeFile("options.json", object.toString(), false);
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
                if(FileManager.exists("options.json")) {
                    instance.save();
                }
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
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("General always specific options for CodeClient"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("CodeClient API"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Allows external apps to add code to your plot."))
                                        .text(Text.literal("(requires restart)"))
                                        .build())
                                .binding(
                                        false,
                                        () -> CodeClientAPI,
                                        opt -> CodeClientAPI = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .flag(OptionFlag.GAME_RESTART)
                                .build())
                        .option(Option.createBuilder(CharSetOption.class)
                                .name(Text.literal("Read Charset"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Can fix artifacts in ActionDump loading."))
                                        .build())
                                .binding(
                                        CharSetOption.UTF_8,
                                        () -> FileCharSet,
                                        opt -> FileCharSet = opt
                                )
                                .flag(minecraftClient -> ActionDump.clear())
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, CharSetOption.class))
                                .build())
                        .option(Option.createBuilder(CharSetOption.class)
                                .name(Text.literal("Save Charset"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("When getting the actiondump get it in a needed format."),Text.literal("Default recommended."))
                                        .build())
                                .binding(
                                        CharSetOption.UTF_8,
                                        () -> SaveCharSet,
                                        opt -> SaveCharSet = opt
                                )
                                .flag(minecraftClient -> ActionDump.clear())
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, CharSetOption.class))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto Fly"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Automatically runs /fly when you go to spawn."))
                                        .build())
                                .binding(
                                        false,
                                        () -> AutoFly,
                                        opt -> AutoFly = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Dev Mode"))
                        .tooltip(Text.literal("Customize how you code."))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Navigation"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("How you move about."))
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("NoClip"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("If you can NoClip in the dev space."))
                                                .build())
                                        .binding(
                                                true,
                                                () -> NoClipEnabled,
                                                option -> NoClipEnabled = option
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.literal("AirStrafe Modifier"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("How much faster you go when jumping in dev space."))
                                                .text(Text.literal("Your jump speed will be based of walking speed"))
                                                .build())
                                        .binding(
                                                10,
                                                () -> AirSpeed,
                                                opt -> AirSpeed = opt
                                        )
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 30)
                                                .step(1))
                                        .build())
//                                .option(Option.createBuilder(boolean.class)
//                                        .name(Text.literal("Air Control"))
//                                        .description(OptionDescription.createBuilder()
//                                                .text(Text.literal("Gives you the same control in air as walking."))
//                                                .build())
//                                        .binding(
//                                                false,
//                                                () -> AirControl,
//                                                opt -> AirControl = opt
//                                        )
//                                        .controller(TickBoxController::new)
//                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Interaction"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Customize how you actually interact with code, placing and breaking code blocks."))
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Custom Block Interactions"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Hides the local block when placing, and hides a codeblock on breaking."))
                                                .text(Text.literal("This can allow faster placing of blocks."))
                                                .build())
                                        .binding(
                                                true,
                                                () -> CustomBlockInteractions,
                                                opt -> CustomBlockInteractions = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Custom Tag Interaction"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Toggle \"faster\" block tags, with interactions client handled."))
                                                .build())
                                        .binding(
                                                false,
                                                () -> CustomTagInteraction,
                                                opt -> CustomTagInteraction = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Place on Air"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Allows you to place on air where codespaces would go."))
                                                .text(Text.literal("This will interfere with other players who don't have this mod, such as helpers!").formatted(Formatting.YELLOW,Formatting.BOLD))
                                                .build())
                                        .binding(
                                                false,
                                                () -> PlaceOnAir,
                                                opt -> PlaceOnAir = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(float.class)
                                        .name(Text.literal("Reach Distance"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Extend your reach distance, moving you forward if you can't reach."))
                                                .build())
                                        .binding(
                                                5f,
                                                () -> ReachDistance,
                                                opt -> ReachDistance = opt
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(5f, 10f)
                                                .step(0.1f))
                                        .available(false)
                                        .build())
                                .option(Option.createBuilder(LayerInteractionMode.class)
                                        .name(Text.literal("Layer Interaction"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.literal("Controls how you interact with code layers, including ones that don't exist."))
                                                .text(Text.of(CodeLayerInteractionMode.description))
                                                .build())
                                        .binding(
                                                LayerInteractionMode.AUTO,
                                                () -> CodeLayerInteractionMode,
                                                opt -> CodeLayerInteractionMode = opt
                                        )
                                        .controller(nodeOption -> () -> new EnumController<>(nodeOption, LayerInteractionMode.class))
                                        .build())
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto Focus Search"))
                                .description(OptionDescription.of(Text.literal("When opening the Code Palette (").append(Text.keybind("key.codeclient.actionpallete")).append(") automatically select the search bar."),Text.literal("This is disabled because it interferes with navigation binds.")))
                                .binding(
                                        false,
                                        () -> FocusSearch,
                                        opt -> FocusSearch = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("CCDBUG"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Toggle CCDBUG, which is a variable and entity watcher for debugging."))
                                        .build())
                                .binding(
                                        true,
                                        () -> CCDBUG,
                                        opt -> CCDBUG = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Show Invisible Blocks"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Show blocks like barriers and other invisible blocks whilst building or coding."))
                                        .build())
                                .binding(
                                        false,
                                        () -> InvisibleBlocksInDev,
                                        opt -> InvisibleBlocksInDev = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .available(false)
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("AutoJoin"))
                        .tooltip(Text.literal("If and where to auto join."))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Enabled"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("If CodeClient should automatically connect you to DF."))
                                        .build())
                                .binding(
                                        false,
                                        () -> AutoJoin,
                                        opt -> AutoJoin = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Node.class)
                                .name(Text.literal("Node"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Enable the above option."))
                                        .build())
                                .binding(
                                        Node.None,
                                        () -> AutoNode,
                                        opt -> AutoNode = opt
                                )
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, Node.class))
//                                .available(AutoJoin)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto Join Plot"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("When you connect, automatically run /join."))
                                        .text(Text.literal("If you want a plot on beta, make sure you set the node."))
                                        .build())
                                .binding(
                                        false,
                                        () -> AutoJoinPlot,
                                        opt -> AutoJoinPlot = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.literal("Plot ID"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Enable the above option."))
                                        .build())
                                .binding(
                                        0,
                                        () -> AutoJoinPlotId,
                                        opt -> AutoJoinPlotId = opt
                                )
                                .controller(IntegerFieldControllerBuilder::create)
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
        OFF("No changes."),
        AUTO("Code layers are custom when you are above them."),
        ON("Code layers are always custom.");

        public final String description;
        LayerInteractionMode(String description) {
            this.description = description;
        }
    }

    public Config() {
    }
}
