package dev.dfonline.codeclient.config;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
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
    public boolean NoClipEnabled = false;
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
    public boolean AutoFly = false;
    public LayerInteractionMode CodeLayerInteractionMode = LayerInteractionMode.AUTO;
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
    public boolean DevForBuild = false;
    public boolean ChatEditsVars = true;
    public boolean InsertOverlay = true;
    public boolean ParameterGhosts = true;
    public boolean ActionViewer = true;
    public boolean InvertActionViewerScroll = false;
    public ActionViewerAlignment ActionViewerLocation = ActionViewerAlignment.TOP;
    public int RecentValues = 0;
    public Boolean ValueDetails = true;
    public Boolean PhaseToggle = false;
    public DestroyItemReset DestroyItemResetMode = DestroyItemReset.OFF;
    public boolean ShowVariableScopeBelowName = true;
    public boolean DevNodes = false;
    public boolean GiveUuidNameStrings = true;
    public boolean CPUDisplay = true;
    public CPUDisplayCornerOption CPUDisplayCorner = CPUDisplayCornerOption.TOP_LEFT;
    public boolean HideScopeChangeMessages = true;
    public boolean HighlighterEnabled = true;
    public boolean HighlightExpressions = true;
    public boolean HighlightMiniMessage = true;
    public int MiniMessageTagColor = 0x808080;

    public Config() {
    }

    public static Config getConfig() {
        if (instance == null) {
            try {
                instance = CodeClient.gson.fromJson(FileManager.readConfig(), Config.class);
            } catch (Exception exception) {
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

    public void save() {
        try {
            JsonObject object = new JsonObject();
            object.addProperty("NoClipEnabled", NoClipEnabled);
            object.addProperty("AirSpeed", AirSpeed);
            object.addProperty("PlaceOnAir", PlaceOnAir);
            object.addProperty("CodeClientAPI", CodeClientAPI);
            object.addProperty("CCDBUG", CCDBUG);
            object.addProperty("CustomBlockInteractions", CustomBlockInteractions);
            object.addProperty("CustomTagInteraction", CustomTagInteraction);
            object.addProperty("AutoJoin", AutoJoin);
            object.addProperty("AutoNode", AutoNode.name());
            object.addProperty("AutoJoinPlot", AutoJoinPlot);
            object.addProperty("AutoJoinPlotId", AutoJoinPlotId);
            object.addProperty("FileCharSet", FileCharSet.name());
            object.addProperty("InvisibleBlocksInDev", InvisibleBlocksInDev);
            object.addProperty("AutoFly", AutoFly);
            object.addProperty("CodeLayerInteractionMode", CodeLayerInteractionMode.name());
            object.addProperty("FocusSearch", FocusSearch);
            object.addProperty("SaveCharSet", SaveCharSet.name());
            object.addProperty("RecentChestInsert", RecentChestInsert);
            object.addProperty("HighlightChestsWithAir", HighlightChestsWithAir);
            object.addProperty("HighlightChestDuration", HighlightChestDuration);
            object.addProperty("ChestHighlightColor", ChestHighlightColor);
            object.addProperty("UseIForLineScope", UseIForLineScope);
            object.addProperty("CustomBlockBreaking", CustomBlockBreaking);
            object.addProperty("ChestPeeker", ChestPeeker);
            object.addProperty("ChestPeekerX", ChestPeekerX);
            object.addProperty("ChestPeekerY", ChestPeekerY);
            object.addProperty("ReportBrokenBlock", ReportBrokenBlock);
            object.addProperty("ScopeSwitcher", ScopeSwitcher);
            object.addProperty("UpAngle", UpAngle);
            object.addProperty("DownAngle", DownAngle);
            object.addProperty("TeleportUp", TeleportUp);
            object.addProperty("TeleportDown", TeleportDown);
            object.addProperty("Line1Color", Line1Color);
            object.addProperty("Line2Color", Line2Color);
            object.addProperty("Line3Color", Line3Color);
            object.addProperty("UseSelectionColor", UseSelectionColor);
            object.addProperty("Line4Color", Line4Color);
            object.addProperty("SignPeeker", SignPeeker);
            object.addProperty("CustomCodeChest", CustomCodeChest.name());
            object.addProperty("PickAction", PickAction);
            object.addProperty("DevForBuild", DevForBuild);
            object.addProperty("ChatEditsVars",ChatEditsVars);
            object.addProperty("InsertOverlay",InsertOverlay);
            object.addProperty("ParameterGhosts",ParameterGhosts);
            object.addProperty("ActionViewer",ActionViewer);
            object.addProperty("InvertActionViewerScroll",InvertActionViewerScroll);
            object.addProperty("ActionViewerLocation",ActionViewerLocation.name());
            object.addProperty("RecentValues", RecentValues);
            object.addProperty("PhaseToggle", PhaseToggle);
            object.addProperty("DestroyItemResetMode", DestroyItemResetMode.name());
            object.addProperty("ShowVariableScopeBelowName", ShowVariableScopeBelowName);
            object.addProperty("DevNodes", DevNodes);
            object.addProperty("GiveUuidNameStrings", GiveUuidNameStrings);
            object.addProperty("CPUDisplay", CPUDisplay);
            object.addProperty("CPUDisplayCorner", CPUDisplayCorner.name());
            object.addProperty("HideScopeChangeMessages", HideScopeChangeMessages);

            object.addProperty("HighlighterEnabled", HighlighterEnabled);
            object.addProperty("HighlightExpressions", HighlightExpressions);
            object.addProperty("HighlightMiniMessage", HighlightMiniMessage);
            object.addProperty("MiniMessageTagColor", MiniMessageTagColor);

            FileManager.writeConfig(object.toString());
        } catch (Exception e) {
            CodeClient.LOGGER.info("Couldn't save config: " + e);
        }
    }

    public YetAnotherConfigLib getLibConfig() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("codeclient.config"))
                //<editor-fold desc="General">
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("codeclient.config.tab.general"))
                        .tooltip(Text.translatable("codeclient.config.tab.general.tooltip"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.api"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.api.description"))
                                        .text(Text.translatable("codeclient.config.requires_restart"))
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
                                .name(Text.translatable("codeclient.config.read_charset"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.read_charset.description"))
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
                                .name(Text.translatable("codeclient.config.save_charset"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.save_charset.description"), Text.translatable("codeclient.config.default_recommended"))
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
                                .name(Text.translatable("codeclient.config.dev_for_build"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.dev_for_build.description")))
                                .binding(
                                        false,
                                        () -> DevForBuild,
                                        opt -> DevForBuild = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.auto_focus_search"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.auto_focus_search.description", Text.keybind("key.codeclient.codepalette")), Text.translatable("codeclient.config.auto_focus_search.description.2")))
                                .binding(
                                        false,
                                        () -> FocusSearch,
                                        opt -> FocusSearch = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.chat_edits_vars"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.chat_edits_vars.description")))
                                .binding(
                                        true,
                                        () -> ChatEditsVars,
                                        opt -> ChatEditsVars = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.ccdbug"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.ccdbug.description"))
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
                                .name(Text.translatable("codeclient.config.autojoin"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.autojoin.description")))
                                .collapsed(true)
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.translatable("codeclient.config.autojoin.enabled"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.autojoin.enabled.description"))
                                                .build())
                                        .binding(
                                                false,
                                                () -> AutoJoin,
                                                opt -> AutoJoin = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Node.class)
                                        .name(Text.translatable("codeclient.config.autojoin.node"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.autojoin.node.description"))
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
                                        .name(Text.translatable("codeclient.config.autojoin.plot"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.autojoin.plot.description1"))
                                                .text(Text.translatable("codeclient.config.autojoin.plot.description2"))
                                                .build())
                                        .binding(
                                                false,
                                                () -> AutoJoinPlot,
                                                opt -> AutoJoinPlot = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.translatable("codeclient.config.autojoin.plotid"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.autojoin.plotid.description"))
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
                        .name(Text.translatable("codeclient.config.tab.navigation"))
                        .tooltip(Text.translatable("codeclient.config.tab.navigation.description"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.noclip"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.noclip.description"))
                                        .build())
                                .binding(
                                        false,
                                        () -> NoClipEnabled,
                                        opt -> NoClipEnabled = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("codeclient.config.airstrafe_modifier"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.airstrafe_modifier.description1"))
                                        .text(Text.translatable("codeclient.config.airstrafe_modifier.description2"))
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
                                .name(Text.translatable("codeclient.config.angle_up"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.angle_up.description"), Text.translatable("codeclient.config.requires_noclip")))
                                .binding(
                                        50F,
                                        () -> UpAngle,
                                        opt -> UpAngle = opt
                                )
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0F, 180F).step(1F))
                                .build())
                        .option(Option.createBuilder(float.class)
                                .name(Text.translatable("codeclient.config.angle_down"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.angle_down.description"), Text.translatable("codeclient.config.requires_noclip")))
                                .binding(50F,
                                        () -> DownAngle,
                                        opt -> DownAngle = opt
                                )
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0F, 180F).step(1F))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.tp_up"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.tp_up.description"), Text.translatable("codeclient.config.requires_noclip")))
                                .binding(
                                        false,
                                        () -> TeleportUp,
                                        opt -> TeleportUp = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.tp_down"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.tp_down.description"), Text.translatable("codeclient.config.requires_noclip")))
                                .binding(
                                        false,
                                        () -> TeleportDown,
                                        opt -> TeleportDown = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.phase_toggle"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.phase_toggle.description"))
                                        .build())
                                .binding(
                                        false,
                                        () -> PhaseToggle,
                                        opt -> PhaseToggle = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                //</editor-fold>
                //<editor-fold desc="Interaction">
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("codeclient.config.tab.interaction"))
                        .tooltip(Text.translatable("codeclient.config.tab.interaction.description"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.custom_block_interaction"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.custom_block_interaction.description1"))
                                        .text(Text.translatable("codeclient.config.custom_block_interaction.description2"))
                                        .build())
                                .binding(
                                        true,
                                        () -> CustomBlockInteractions,
                                        opt -> CustomBlockInteractions = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.custom_block_breaking"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.custom_block_breaking.description1"), Text.translatable("codeclient.config.custom_block_breaking.description2"), Text.translatable("codeclient.config.custom_block_breaking.description3")))
                                .binding(
                                        false,
                                        () -> CustomBlockBreaking,
                                        opt -> CustomBlockBreaking = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.custom_tag_interaction"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.custom_tag_interaction.description"))
                                        .build())
                                .binding(
                                        false,
                                        () -> CustomTagInteraction,
                                        opt -> CustomTagInteraction = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.insert_overlay"))
                                .description(OptionDescription.of(
                                        Text.translatable("codeclient.config.insert_overlay.description")
                                ))
                                .binding(
                                        true,
                                        () -> InsertOverlay,
                                        opt -> InsertOverlay = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.createBuilder(CustomChestMenuType.class)
                                .name(Text.translatable("codeclient.config.custom_code_chest_menu"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.custom_code_chest_menu.description"))
                                        .text(Text.of(CustomCodeChest.description))
                                        .text(Text.literal("THIS IS IN BETA!").formatted(Formatting.YELLOW, Formatting.BOLD))
                                        .build())
                                .binding(
                                        CustomChestMenuType.OFF,
                                        () -> CustomCodeChest,
                                        opt -> CustomCodeChest = opt
                                )
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, CustomChestMenuType.class))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.place_on_air"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.place_on_air.description1"))
                                        .text(Text.translatable("codeclient.config.place_on_air.description2").formatted(Formatting.YELLOW, Formatting.BOLD))
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
                                .name(Text.translatable("codeclient.config.scope_switcher"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.scope_switcher.description")))
                                .binding(
                                        true,
                                        () -> ScopeSwitcher,
                                        opt -> ScopeSwitcher = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.report_broken_blocks"))
                                .description(
                                        OptionDescription.of(Text.translatable("codeclient.config.report_broken_blocks.description1"), Text.translatable("codeclient.config.report_broken_blocks.description2")))
                                .binding(
                                        true,
                                        () -> ReportBrokenBlock,
                                        opt -> ReportBrokenBlock = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(LayerInteractionMode.class)
                                .name(Text.translatable("codeclient.config.layer_interaction"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.layer_interaction.description"))
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
                                .name(Text.translatable("codeclient.config.pick_block_action"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.pick_block_action.description1"), Text.translatable("codeclient.config.pick_block_action.description2").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/DFOnline/CodeClient/wiki/actiondump")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.config.pick_block_action.description3"))).withUnderline(true).withColor(Formatting.AQUA))))
                                .binding(
                                        true,
                                        () -> PickAction,
                                        opt -> PickAction = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(int.class)
                                .name(Text.translatable("codeclient.config.recent_values"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.recent_values.description")))
                                .binding(
                                        0,
                                        () -> RecentValues,
                                        opt -> RecentValues = opt
                                )
                                .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(0, 100))
                                .build())
                        .option(Option.createBuilder(DestroyItemReset.class)
                                .name(Text.translatable("codeclient.config.destroy_item_reset.name"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.destroy_item_reset.description")))
                                .binding(
                                        DestroyItemReset.OFF,
                                        () -> DestroyItemResetMode,
                                        opt -> DestroyItemResetMode = opt
                                )
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, DestroyItemReset.class))
                                .build())
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.translatable("codeclient.config.givestrings"))
                                .description(OptionDescription.createBuilder()
                                        .text(Text.translatable("codeclient.config.givestrings.description"))
                                        .build())
                                .binding(
                                        true,
                                        () -> GiveUuidNameStrings,
                                        opt -> GiveUuidNameStrings = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                //</editor-fold>
                //<editor-fold desc="Visual">
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("codeclient.config.tab.visual"))
                        .tooltip(Text.translatable("codeclient.config.tab.visual.description"))
                        //<editor-fold desc="Ungrouped">
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.show_invisible_blocks"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.show_invisible_blocks.description1"), Text.translatable("codeclient.config.show_invisible_blocks.description2")))
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
                                .name(Text.translatable("codeclient.config.show_variable_scope_below_name"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.show_variable_scope_below_name.description")))
                                .binding(
                                        true,
                                        () -> ShowVariableScopeBelowName,
                                        opt -> ShowVariableScopeBelowName = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.show_i_on_line_scope"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.show_i_on_line_scope.description")))
                                .binding(
                                        false,
                                        () -> UseIForLineScope,
                                        opt -> UseIForLineScope = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.show_sign_text_from_behind"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.show_sign_text_from_behind.description")))
                                .binding(
                                        true,
                                        () -> SignPeeker,
                                        opt -> SignPeeker = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.param_ghosts"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.param_ghosts.description")))
                                .binding(
                                        true,
                                        () -> ParameterGhosts,
                                        opt -> ParameterGhosts = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.value_details"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.value_details.description")))
                                .binding(
                                        true,
                                        () -> ValueDetails,
                                        opt -> ValueDetails = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.cpu_display"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.cpu_display.description")))
                                .binding(
                                        true,
                                        () -> CPUDisplay,
                                        opt -> CPUDisplay = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.createBuilder(CPUDisplayCornerOption.class)
                                .name(Text.translatable("codeclient.config.cpu_display_corner.name"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.cpu_display_corner.description")))
                                .binding(
                                        CPUDisplayCornerOption.TOP_LEFT,
                                        () -> CPUDisplayCorner,
                                        opt -> CPUDisplayCorner = opt
                                )
                                .controller(nodeOption -> () -> new EnumController<>(nodeOption, CPUDisplayCornerOption.class))
                                .build())
                        .option(Option.createBuilder(Boolean.class)
                                .name(Text.translatable("codeclient.config.hide_scope_change_messages"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.hide_scope_change_messages.description")))
                                .binding(
                                        true,
                                        () -> HideScopeChangeMessages,
                                        opt -> HideScopeChangeMessages = opt
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        //</editor-fold>
                        //<editor-fold desc="Action Viewer">
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("codeclient.config.action_viewer"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.action_viewer.description")))
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.action_viewer.enable"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.action_viewer.enable.description")))
                                        .binding(
                                                true,
                                                () -> ActionViewer,
                                                opt -> ActionViewer = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.action_viewer.invert_scroll"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.action_viewer.invert_scroll.description")))
                                        .binding(
                                                false,
                                                () -> InvertActionViewerScroll,
                                                opt -> InvertActionViewerScroll = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(ActionViewerAlignment.class)
                                        .name(Text.translatable("codeclient.config.action_viewer.alignment"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.action_viewer.alignment.description")))
                                        .binding(
                                                ActionViewerAlignment.TOP,
                                                () -> ActionViewerLocation,
                                                opt -> ActionViewerLocation = opt
                                        )
                                        .controller(nodeOption -> () -> new EnumController<>(nodeOption, ActionViewerAlignment.class))
                                        .build())
                                .build())
                        //</editor-fold>
                        //<editor-fold desc="Chest Preview">
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("codeclient.config.chest_preview"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.chest_preview.description")))
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chest_preview.enable"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.chest_preview.enable.description")))
                                        .binding(
                                                true,
                                                () -> ChestPeeker,
                                                opt -> ChestPeeker = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.translatable("codeclient.config.chest_preview.x_offset"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.chest_preview.x_offset.description")))
                                        .binding(
                                                0,
                                                () -> ChestPeekerX,
                                                opt -> ChestPeekerX = opt
                                        )
                                        .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(-500, 500))
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.translatable("codeclient.config.chest_preview.y_offset"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.chest_preview.y_offset.description")))
                                        .binding(
                                                -4,
                                                () -> ChestPeekerY,
                                                opt -> ChestPeekerY = opt
                                        )
                                        .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(-500, 500))
                                        .build())
                                .build())
                        //</editor-fold>
                        //<editor-fold desc="Chat Highlighter">
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("codeclient.config.chat_highlighter"))
                                .description(OptionDescription.of(
                                        Text.translatable("codeclient.config.chat_highlighter.description"),
                                        Text.translatable("codeclient.config.chat_highlighter.description.extra")
                                ))
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chat_highlighter.enable"))
                                        .description(OptionDescription.of(
                                                Text.translatable("codeclient.config.chat_highlighter.enable.description"),
                                                Text.empty(),
                                                Text.translatable("codeclient.config.chat_highlighter.description").setStyle(Style.EMPTY.withColor(Formatting.GRAY)),
                                                Text.translatable("codeclient.config.chat_highlighter.description.extra").setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                                        ))
                                        .binding(
                                                true,
                                                () -> HighlighterEnabled,
                                                opt -> HighlighterEnabled = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chat_highlighter.with_expressions"))
                                        .description(OptionDescription.of(
                                                Text.translatable("codeclient.config.chat_highlighter.with_expressions.description"),
                                                Text.translatable("codeclient.config.chat_highlighter.with_expressions.description.example")
                                        ))
                                        .binding(
                                                true,
                                                () -> HighlightExpressions,
                                                opt -> HighlightExpressions = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chat_highlighter.with_minimessage"))
                                        .description(OptionDescription.of(
                                                Text.translatable("codeclient.config.chat_highlighter.with_minimessage.description1"),
                                                Text.translatable("codeclient.config.chat_highlighter.with_minimessage.description2")
                                        ))
                                        .binding(
                                                true,
                                                () -> HighlightMiniMessage,
                                                opt -> HighlightMiniMessage = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.chat_highlighter.minimessage_tag_color"))
                                        .description(OptionDescription.of(
                                                Text.translatable("codeclient.config.chat_highlighter.minimessage_tag_color.description")
                                        ))
                                        .binding(
                                                new Color((float) 128 / 255, (float) 128 / 255, (float) 128 / 255),
                                                () -> new Color(MiniMessageTagColor),
                                                opt -> MiniMessageTagColor = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        //</editor-fold>
                        //<editor-fold desc="Chest Highlight">
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("codeclient.config.chest_highlight"))
                                .description(OptionDescription.of(
                                        Text.translatable("codeclient.config.chest_highlight.description1"),
                                        Text.translatable("codeclient.config.chest_highlight.description2")))
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chest_highlight.enable"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.chest_highlight.enable.description"))
                                                .build())
                                        .binding(
                                                true,
                                                () -> RecentChestInsert,
                                                opt -> RecentChestInsert = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Boolean.class)
                                        .name(Text.translatable("codeclient.config.chest_highlight.with_empty_hand"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.chest_highlight.with_empty_hand.description")))
                                        .binding(
                                                false,
                                                () -> HighlightChestsWithAir,
                                                opt -> HighlightChestsWithAir = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(int.class)
                                        .name(Text.translatable("codeclient.config.chest_highlight.duration"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.chest_highlight.duration.description1"), Text.translatable("codeclient.config.chest_highlight.duration.description2")))
                                        .binding(
                                                10,
                                                () -> HighlightChestDuration,
                                                opt -> HighlightChestDuration = opt
                                        )
                                        .controller(integerOption -> new IntegerFieldControllerBuilderImpl(integerOption).min(1))
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.chest_highlight.color"))
                                        .description(OptionDescription.createBuilder()
                                                .text(Text.translatable("codeclient.config.chest_highlight.color.description"))
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
                                .name(Text.translatable("codeclient.config.sign_color"))
                                .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.description1"), Text.translatable("codeclient.config.sign_color.description2")))
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.sign_color.line1"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.line1.description1"), Text.translatable("codeclient.config.sign_color.line1.description2")))
                                        .binding(
                                                new Color(0xAAAAAA),
                                                () -> new Color(Line1Color),
                                                opt -> Line1Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.sign_color.line2"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.line2.description1"), Text.translatable("codeclient.config.sign_color.line2.description2")))
                                        .binding(
                                                new Color(0xC5C5C5),
                                                () -> new Color(Line2Color),
                                                opt -> Line2Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.sign_color.line3"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.line3.description1"), Text.translatable("codeclient.config.sign_color.line3.description2")))
                                        .binding(
                                                new Color(0xAAFFAA),
                                                () -> new Color(Line3Color),
                                                opt -> Line3Color = opt.getRGB()
                                        )
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(boolean.class)
                                        .name(Text.translatable("codeclient.config.sign_color.selection"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.selection.description")))
                                        .binding(
                                                true,
                                                () -> UseSelectionColor,
                                                opt -> UseSelectionColor = opt
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.createBuilder(Color.class)
                                        .name(Text.translatable("codeclient.config.sign_color.line4"))
                                        .description(OptionDescription.of(Text.translatable("codeclient.config.sign_color.line4.description1"), Text.translatable("codeclient.config.sign_color.line4.description2")))
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
        OFF("No changes.", null),
        SMALL("A small menu with a few lines for editing code values", CustomChestNumbers.SMALL),
        LARGE("A large menu with lots of lines for editing values, and extra slots showing values without editable lines.", CustomChestNumbers.LARGE);

        public final String description;
        public final CustomChestNumbers size;

        CustomChestMenuType(String description, CustomChestNumbers size) {
            this.description = description;
            this.size = size;
        }
    }

    public enum ActionViewerAlignment {
        CENTER,
        TOP
    }

    public enum DestroyItemReset {
        OFF(null),
        STANDARD("rs"),
        COMPACT("rc");

        public String command;
        DestroyItemReset(String command) {
            this.command = command;
        }
    }

    public enum CPUDisplayCornerOption {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
