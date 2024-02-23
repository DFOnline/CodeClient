package dev.dfonline.codeclient.config;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestHandler;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestNumbers;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.impl.controller.IntegerFieldControllerBuilderImpl;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
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
    public boolean RecentChestInsert = true;
    public int ChestHighlightColor = 0x51FFFF;
    public boolean HighlightChestsWithAir = false;
    public int HighlightChestDuration = 10;
    public boolean UseIForLineScope = false;
    public boolean CustomBlockBreaking = false;
    public boolean ChestPeeker = true;
    public int ChestPeekerX = 0;
    public int ChestPeekerY = -4;
    public boolean ReportBrokenBlock = true;
    public boolean ScopeSwitcher = true;
    public float UpAngle = 50;
    public float DownAngle = 50;
    public boolean TeleportUp = false;
    public boolean TeleportDown = false;
    public int Line1Color = 0xAAAAAA;
    public int Line2Color = 0xC5C5C5;
    public int Line3Color = 0xAAFFAA;
    public boolean UseSelectionColor = true;
    public int Line4Color = 0xFF8800;
    public boolean SignPeeker = true;
    public CustomChestMenuType CustomCodeChest = CustomChestMenuType.OFF;
    public boolean PickAction = true;

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
            object.addProperty("RecentChestInsert",RecentChestInsert);
            object.addProperty("HighlightChestsWithAir",HighlightChestsWithAir);
            object.addProperty("HighlightChestDuration",HighlightChestDuration);
            object.addProperty("ChestHighlightColor",ChestHighlightColor);
            object.addProperty("UseIForLineScope",UseIForLineScope);
            object.addProperty("CustomBlockBreaking",CustomBlockBreaking);
            object.addProperty("ChestPeeker",ChestPeeker);
            object.addProperty("ChestPeekerX",ChestPeekerX);
            object.addProperty("ChestPeekerY",ChestPeekerY);
            object.addProperty("ReportBrokenBlock",ReportBrokenBlock);
            object.addProperty("ScopeSwitcher",ScopeSwitcher);
            object.addProperty("UpAngle",UpAngle);
            object.addProperty("DownAngle",DownAngle);
            object.addProperty("TeleportUp",TeleportUp);
            object.addProperty("TeleportDown",TeleportDown);
            object.addProperty("Line1Color",Line1Color);
            object.addProperty("Line2Color",Line2Color);
            object.addProperty("Line3Color",Line3Color);
            object.addProperty("UseSelectionColor",UseSelectionColor);
            object.addProperty("Line4Color",Line4Color);
            object.addProperty("SignPeeker",SignPeeker);
            object.addProperty("CustomCodeChest",CustomCodeChest.name());
            object.addProperty("PickAction",PickAction);
            FileManager.writeConfig(object.toString());
        } catch (Exception e) {
            CodeClient.LOGGER.info("Couldn't save config: " + e);
        }
    }

    public static Config getConfig() {
        if(instance == null) {
            try {
                instance = CodeClient.gson.fromJson(FileManager.readConfig(), Config.class);
            }
            catch (Exception exception) {
                CodeClient.LOGGER.info("Config didn't load: " + exception);
                CodeClient.LOGGER.info("Making a new one.");
                instance = new Config();
                instance.save();
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
                //<editor-fold desc="General">
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
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Auto Focus Search"))
                                .description(OptionDescription.of(Text.literal("When opening the Code Palette (").append(Text.keybind("key.codeclient.codepalette")).append(") automatically select the search bar."),Text.literal("This is disabled because it interferes with navigation binds.")))
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
                        //<editor-fold desc="AutoJoin">
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("AutoJoin"))
                                .description(OptionDescription.of(Text.literal("If and where to auto join.")))
                                .collapsed(true)
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
                        //</editor-fold>
                        .build())
                //</editor-fold>
                //<editor-fold desc="Navigation">
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Navigation"))
                        .tooltip(Text.literal("How you move about in the codespace and the likes."))
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
                        .option(Option.createBuilder(float.class)
                                .name(Text.literal("Angle to go up"))
                                .description(OptionDescription.of(Text.literal("When facing up, within this angle, jumping will take you up a layer")))
                                .binding(
                                        50F,
                                        () -> UpAngle,
                                        opt -> UpAngle = opt
                                )
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0F,180F).step(1F))
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.literal("Angle to go down"))
                                .description(OptionDescription.of(Text.literal("When facing down, within this angle, crouching will take you down a layer")))
                                .binding(50F,
                                        () -> DownAngle,
                                        opt -> DownAngle = opt
                                )
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0F,180F).step(1F))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Teleport Up"))
                                .description(OptionDescription.of(Text.literal("When jumping and facing up, you will get teleported to the layer above")))
                                .binding(
                                        false,
                                        () -> TeleportUp,
                                        opt -> TeleportUp = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Teleport Down"))
                                .description(OptionDescription.of(Text.literal("When crouching and facing down, you will get teleported to the layer underneath")))
                                .binding(
                                        false,
                                        () -> TeleportDown,
                                        opt -> TeleportDown = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
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
                //</editor-fold>
                //<editor-fold desc="Interaction">
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Interaction"))
                        .tooltip(Text.literal("Customize how you actually interact with code, placing and breaking code blocks."))
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
                                .name(Text.literal("Custom Block Breaking"))
                                .description(OptionDescription.of(Text.literal("Keep codeblocks safe from accidental breaking."),Text.literal("They will have the survival breaking animation."),Text.literal("They will take 5 ticks to break (0.25 seconds)")))
                                .binding(
                                        false,
                                        () -> CustomBlockBreaking,
                                        opt -> CustomBlockBreaking = opt
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
                        .option(Option.createBuilder(CustomChestMenuType.class)
                                .name(Text.literal("Custom Code Chest Menu"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.literal("Use a custom menu for chests, with value text boxes for quick editing."))
                                        .text(Text.of(CustomCodeChest.description))
                                        .text(Text.literal("THIS IS IN BETA!").formatted(Formatting.YELLOW,Formatting.BOLD))
                                        .build())
                                .binding(
                                        CustomChestMenuType.OFF,
                                        () -> CustomCodeChest,
                                        opt -> CustomCodeChest = opt
                                )
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, CustomChestMenuType.class))
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
//                                .option(Option.createBuilder(float.class)
//                                        .name(Text.literal("Reach Distance"))
//                                        .description(OptionDescription.createBuilder()
//                                                .text(Text.literal("Extend your reach distance, moving you forward if you can't reach."))
//                                                .build())
//                                        .binding(
//                                                5f,
//                                                () -> ReachDistance,
//                                                opt -> ReachDistance = opt
//                                        )
//                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
//                                                .range(5f, 10f)
//                                                .step(0.1f))
//                                        .available(false)
//                                        .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Scope Switcher"))
                                .description(OptionDescription.of(Text.literal("Right click variables to change their scope with a custom UI.")))
                                .binding(
                                        true,
                                        () -> ScopeSwitcher,
                                        opt -> ScopeSwitcher = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("Report Broken Blocks"))
                                .description(
                                        OptionDescription.of(Text.literal("Get a message on breaking a block telling you what it did."),
                                                Text.literal("Only works for Player Event, Entity Event, Function, Call Function, Process, and Start Process.")))
                                .binding(
                                        true,
                                        () -> ReportBrokenBlock,
                                        opt -> ReportBrokenBlock = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
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
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Pick Block Action"))
                                .description(OptionDescription.of(Text.literal("Replaces pick block with the action"),Text.literal("Requires actiondump.").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://github.com/DFOnline/CodeClient/wiki/actiondump")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Text.literal("Click to open link."))).withUnderline(true).withColor(Formatting.AQUA))))
                                .binding(
                                        true,
                                        () -> PickAction,
                                        opt -> PickAction = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                //</editor-fold>
                //<editor-fold desc="Visual">
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Visual"))
                        .tooltip(Text.literal("Purely visual elements of CodeClient, which can help in coding with information."))
                        //<editor-fold desc="Ungrouped">
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Show Invisible Blocks"))
                                .description(OptionDescription.of(Text.literal("Show blocks like barriers and other invisible blocks whilst building or coding."),
                                        Text.literal("Laggy as of now")))
                                .binding(
                                        false,
                                        () -> InvisibleBlocksInDev,
                                        opt -> InvisibleBlocksInDev = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .available(true)
                                .flag(OptionFlag.RELOAD_CHUNKS)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Show I On Line Scope"))
                                .description(OptionDescription.of(Text.literal("Whenever LINE is shortened, shorten it to I.")))
                                .binding(
                                        false,
                                        () -> UseIForLineScope,
                                        opt -> UseIForLineScope = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.literal("Show sign text from behind"))
                                .description(OptionDescription.of(Text.literal("Show the text on a sign when looking at a codeblock from behind in a popup.")))
                                .binding(
                                        true,
                                        () -> SignPeeker,
                                        opt -> SignPeeker = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        //</editor-fold>
                        //<editor-fold desc="Chest Preview">
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Chest Preview"))
                                .description(OptionDescription.of(Text.literal("An overlay showing the items from inside a chest.")))
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.literal("Preview Chest Contents"))
                                        .description(OptionDescription.of(Text.literal("Show a popup with with item info when looking at a chest.")))
                                        .binding(
                                                true,
                                                () -> ChestPeeker,
                                                opt -> ChestPeeker = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.literal("Preview X Offset"))
                                        .description(OptionDescription.of(Text.literal("How far horizontally the preview popup is")))
                                        .binding(
                                                0,
                                                () -> ChestPeekerX,
                                                opt -> ChestPeekerX = opt
                                        )
                                        .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(-500,500))
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.literal("Preview Y Offset"))
                                        .description(OptionDescription.of(Text.literal("How far vertically the preview popup is")))
                                        .binding(
                                                -4,
                                                () -> ChestPeekerY,
                                                opt -> ChestPeekerY = opt
                                        )
                                        .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(-500,500))
                                        .build())
                                .build())
                        //</editor-fold>
                        //<editor-fold desc="Chest Highlight">
                                .group(OptionGroup.createBuilder()
                                        .name(Text.literal("Chest Highlight"))
                                        .description(OptionDescription.of(
                                                Text.literal("A highlight for inserting items into chests"),
                                                Text.literal("Helpful with finding where you where, or accidentally punching chests.")))
                                        .option(Option.createBuilder(Boolean.class)
                                                .name(Text.literal("Highlight Recent Inserted Chest"))
                                                .description(OptionDescription.createBuilder()
                                                        .text(Text.literal("Highlights the chest you inserted (via punching with an item) an item into"))
                                                        .build())
                                                .binding(
                                                        true,
                                                        () -> RecentChestInsert,
                                                        opt -> RecentChestInsert = opt
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        .option(Option.createBuilder(Boolean.class)
                                                .name(Text.literal("Highlight With Empty Hand"))
                                                .description(OptionDescription.of(Text.literal("If punching a chest with an empty hand should highlight it.")))
                                                .binding(
                                                        false,
                                                        () -> HighlightChestsWithAir,
                                                        opt -> HighlightChestsWithAir = opt
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        .option(Option.createBuilder(int.class)
                                                .name(Text.literal("Highlight Duration"))
                                                .description(OptionDescription.of(Text.literal("How long the highlight should show."),Text.literal("The last second will fade out.")))
                                                .binding(
                                                        10,
                                                        () -> HighlightChestDuration,
                                                        opt -> HighlightChestDuration = opt
                                                )
                                                .controller(integerOption -> new IntegerFieldControllerBuilderImpl(integerOption).min(1))
                                                .build())
                                        .option(Option.createBuilder(Color.class)
                                                .name(Text.literal("Recent Inserted Chest Highlight Color"))
                                                .description(OptionDescription.createBuilder()
                                                        .text(Text.literal("Color of chest highlight"))
                                                        .build())
                                                .binding(
                                                        new Color(0.2F, 1.0F, 1.0F),
                                                        () -> new Color(ChestHighlightColor),
                                                        opt -> ChestHighlightColor = opt.getRGB()
                                                )
                                                .controller(ColorControllerBuilder::create)
                                                .build())
                                        .build())
                        //</editor-fold>
                        //<editor-fold desc="Sign Colors">
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Sign Colors"))
                                .description(OptionDescription.of(Text.literal("Override the color of text on the signs in the dev space."),Text.literal("Using #000000 (pure black) has no effect and disable disable it.")))
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.literal("Line 1"))
                                        .description(OptionDescription.of(Text.literal("The top line on a sign."),Text.literal("Tells you what block it is, such as PLAYER ACTION")))
                                        .binding(
                                                new Color(0xAAAAAA),
                                                () -> new Color(Line1Color),
                                                opt -> Line1Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.literal("Line 2"))
                                        .description(OptionDescription.of(Text.literal("The second line on a sign."),Text.literal("Tells you the action or data, such as SendMessage")))
                                        .binding(
                                                new Color(0xC5C5C5),
                                                () -> new Color(Line2Color),
                                                opt -> Line2Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.literal("Line 3"))
                                        .description(OptionDescription.of(Text.literal("The third line on a sign."),Text.literal("Tells you either the selection or SubAction")))
                                        .binding(
                                                new Color(0xAAFFAA),
                                                () -> new Color(Line3Color),
                                                opt -> Line3Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.literal("Use custom colors for selection"))
                                        .description(OptionDescription.of(Text.literal("Use the selection's color (or a readable similar one) for the color")))
                                        .binding(
                                                true,
                                                () -> UseSelectionColor,
                                                opt -> UseSelectionColor = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.literal("Line 4"))
                                        .description(OptionDescription.of(Text.literal("The bottom line on a sign."),Text.literal("Only tells you if it is inverted: NOT")))
                                        .binding(
                                                new Color(0xFF8800),
                                                () -> new Color(Line4Color),
                                                opt -> Line4Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .build())
                        //</editor-fold>
                        .build())
                //</editor-fold>
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

    public enum CustomChestMenuType {
        OFF("No changes.",null),
        SMALL("A small menu with a few lines for editing code values", CustomChestNumbers.SMALL),
        LARGE("A large menu with lots of lines for editing values, and extra slots showing values without editable lines.", CustomChestNumbers.LARGE);

        public final String description;
        public final CustomChestNumbers size;

        CustomChestMenuType(String description, CustomChestNumbers size) {
            this.description = description;
            this.size = size;
        }
    }

    public Config() {
    }
}
