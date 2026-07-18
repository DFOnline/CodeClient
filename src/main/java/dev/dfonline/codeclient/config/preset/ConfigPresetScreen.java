package dev.dfonline.codeclient.config.preset;

import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigPresetScreen extends Screen {

    private final Screen parent;

    public ConfigPresetScreen(final Screen parent) {
        super(Component.translatable("screen.codeclient.config_preset.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = this.width / 2;
        final int baseY = this.height / 3 + 20;
        final int buttonWidth = 120;
        final int spacing = 10;

        this.addRenderableWidget(createPresetButton(
                "screen.codeclient.config_preset.minimal",
                "screen.codeclient.config_preset.minimal.tooltip",
                centerX - (buttonWidth + spacing) - buttonWidth / 2,
                baseY,
                ConfigPreset.MINIMAL
        ));

        this.addRenderableWidget(createPresetButton(
                "screen.codeclient.config_preset.basic",
                "screen.codeclient.config_preset.basic.tooltip",
                centerX - buttonWidth / 2,
                baseY,
                ConfigPreset.BASIC
        ));

        this.addRenderableWidget(createPresetButton(
                "screen.codeclient.config_preset.full",
                "screen.codeclient.config_preset.full.tooltip",
                centerX + (buttonWidth + spacing) - buttonWidth / 2,
                baseY,
                ConfigPreset.FULL
        ));
    }

    private Button createPresetButton(final String translationKey, final String tooltipKey, final int x, final int y, final ConfigPreset preset) {
        return Button.builder(
                        Component.translatable(translationKey),
                        button -> applyPreset(preset)
                ).size(120, 20)
                .pos(x, y)
                .tooltip(Tooltip.create(Component.translatable(tooltipKey)))
                .build();
    }

    private void applyPreset(final ConfigPreset preset) {
        final Config config = Config.getConfig();
        preset.getApply().accept(config);
        config.HasSelectedPreset = true;
        config.save();

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(final GuiGraphics context, final int mouseX, final int mouseY, final float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int titleY = this.height / 6;
        final Component title = Component.translatable("screen.codeclient.config_preset.title");
        context.drawCenteredString(this.font, title, this.width / 2, titleY, 0xFFFFFFFF);

        final boolean firstRun = !Config.getConfig().HasSelectedPreset;
        final Component mainSubtitle = firstRun
                ? Component.translatable("screen.codeclient.config_preset.welcome.title")
                : Component.translatable("screen.codeclient.config_preset.subtitle");
        final Component secondarySubtitle = firstRun
                ? Component.translatable("screen.codeclient.config_preset.welcome.subtitle")
                : Component.empty();

        final int textY = titleY + 20;
        context.drawCenteredString(this.font, mainSubtitle, this.width / 2, textY, 0xFFA0E6FF);

        if (!secondarySubtitle.getString().isEmpty()) {
            context.drawCenteredString(this.font, secondarySubtitle, this.width / 2, textY + 12, 0xFFA0E6FF);
            context.drawCenteredString(this.font, Component.translatable("screen.codeclient.config_preset.hover_hint"), this.width / 2, textY + 24, 0xFFCCCCCC);
        } else {
            context.drawCenteredString(this.font, Component.translatable("screen.codeclient.config_preset.hover_hint"), this.width / 2, textY + 12, 0xFFCCCCCC);
        }

        if (!firstRun) {
            final Component warning = Component.translatable("screen.codeclient.config_preset.warning");
            context.drawCenteredString(this.font, warning, this.width / 2, this.height / 3, 0xFFFF5555);
        }
    }
}
