package dev.dfonline.codeclient.config.preset;

import dev.dfonline.codeclient.config.Config;

import java.util.function.Consumer;

public enum ConfigPreset {

    MINIMAL(config -> {
        config.NoClipEnabled = false;
        config.PlaceOnAir = false;
        config.CodeClientAPI = true;
        config.CustomBlockBreaking = false;
        config.CustomBlockInteractions = false;
        config.CustomTagInteraction = false;
        config.AutoFly = false;
        config.CodeLayerInteractionMode = Config.LayerInteractionMode.OFF;
        config.RecentChestInsert = false;
        config.ChestPeeker = false;
        config.ReportBrokenBlock = false;
        config.ScopeSwitcher = false;
        config.TeleportUp = false;
        config.TeleportDown = false;
        config.SignPeeker = false;
        config.CustomCodeChest = Config.CustomChestMenuType.OFF;
        config.AdvancedMiddleClick = false;
        config.DevForBuild = false;
        config.ChatEditsVars = true;
        config.InsertOverlay = false;
        config.ParameterGhosts = false;
        config.ActionViewer = true;
        config.RecentValues = 0;
        config.ValueDetails = true;
        config.PhaseToggle = false;
        config.DestroyItemResetMode = Config.DestroyItemReset.OFF;
        config.ShowVariableScopeBelowName = true;
        config.CPUDisplay = true;
        config.CPUDisplayCorner = Config.CPUDisplayCornerOption.TOP_LEFT;
        config.HideScopeChangeMessages = true;
        config.HighlighterEnabled = true;
        config.HighlightExpressions = true;
        config.HighlightMiniMessage = true;
        config.StateSwitcher = false;
        config.SpeedSwitcher = false;
        config.GiveUuidNameStrings = false;
    }),
    BASIC(config ->{
        config.NoClipEnabled = false;
        config.PlaceOnAir = false;
        config.CodeClientAPI = true;
        config.CustomBlockInteractions = false;
        config.CustomTagInteraction = true;
        config.CodeLayerInteractionMode = Config.LayerInteractionMode.AUTO;
        config.RecentChestInsert = true;
        config.ChestPeeker = true;
        config.ScopeSwitcher = true;
        config.TeleportUp = false;
        config.TeleportDown = false;
        config.SignPeeker = true;
        config.CustomCodeChest = Config.CustomChestMenuType.OFF;
        config.AdvancedMiddleClick = false;
        config.ChatEditsVars = true;
        config.InsertOverlay = false;
        config.ParameterGhosts = false;
        config.ActionViewer = true;
        config.ValueDetails = true;
        config.PhaseToggle = true;
        config.DestroyItemResetMode = Config.DestroyItemReset.OFF;
        config.ShowVariableScopeBelowName = true;
        config.CPUDisplay = true;
        config.CPUDisplayCorner = Config.CPUDisplayCornerOption.TOP_LEFT;
        config.HideScopeChangeMessages = true;
        config.HighlighterEnabled = true;
        config.HighlightExpressions = true;
        config.HighlightMiniMessage = true;
        config.StateSwitcher = true;
        config.SpeedSwitcher = true;
        config.GiveUuidNameStrings = true;
    }),
    FULL(config -> {
        config.NoClipEnabled = true;
        config.CodeClientAPI = true;
        config.CustomBlockInteractions = true;
        config.CustomTagInteraction = true;
        config.CodeLayerInteractionMode = Config.LayerInteractionMode.AUTO;
        config.RecentChestInsert = true;
        config.ChestPeeker = true;
        config.ScopeSwitcher = true;
        config.TeleportUp = true;
        config.TeleportDown = true;
        config.SignPeeker = true;
        config.CustomCodeChest = Config.CustomChestMenuType.OFF;
        config.ChatEditsVars = true;
        config.InsertOverlay = true;
        config.ParameterGhosts = true;
        config.ActionViewer = true;
        config.ValueDetails = true;
        config.PhaseToggle = true;
        config.DestroyItemResetMode = Config.DestroyItemReset.OFF;
        config.ShowVariableScopeBelowName = true;
        config.CPUDisplay = true;
        config.CPUDisplayCorner = Config.CPUDisplayCornerOption.TOP_LEFT;
        config.HideScopeChangeMessages = true;
        config.HighlighterEnabled = true;
        config.HighlightExpressions = true;
        config.HighlightMiniMessage = true;
        config.StateSwitcher = true;
        config.SpeedSwitcher = true;
        config.GiveUuidNameStrings = true;
    })
    ;
    
    private final Consumer<Config> apply;
    ConfigPreset(Consumer<Config> apply) {
        this.apply = apply;
    }

    public Consumer<Config> getApply() {
        return apply;
    }
}
