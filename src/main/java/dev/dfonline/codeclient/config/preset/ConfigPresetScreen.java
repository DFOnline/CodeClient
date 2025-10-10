package dev.dfonline.codeclient.config.preset;

import dev.dfonline.codeclient.config.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

public class ConfigPresetScreen extends Screen {

    private final Screen parent;

    public ConfigPresetScreen(final Screen parent) {
        super(Text.translatable("screen.codeclient.config_preset.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        final int centerX = this.width / 2;
        final int baseY = this.height / 3 + 20;
        final int buttonWidth = 120;
        final int spacing = 10;

        this.addDrawableChild(createPresetButton(
                "screen.codeclient.config_preset.minimal",
                "screen.codeclient.config_preset.minimal.tooltip",
                centerX - (buttonWidth + spacing) - buttonWidth / 2,
                baseY,
                ConfigPreset.MINIMAL
        ));

        this.addDrawableChild(createPresetButton(
                "screen.codeclient.config_preset.basic",
                "screen.codeclient.config_preset.basic.tooltip",
                centerX - buttonWidth / 2,
                baseY,
                ConfigPreset.BASIC
        ));

        this.addDrawableChild(createPresetButton(
                "screen.codeclient.config_preset.full",
                "screen.codeclient.config_preset.full.tooltip",
                centerX + (buttonWidth + spacing) - buttonWidth / 2,
                baseY,
                ConfigPreset.FULL
        ));
    }

    private ButtonWidget createPresetButton(final String translationKey, final String tooltipKey, final int x, final int y, final ConfigPreset preset) {
        return ButtonWidget.builder(
                        Text.translatable(translationKey),
                        button -> applyPreset(preset)
                ).size(120, 20)
                .position(x, y)
                .tooltip(Tooltip.of(Text.translatable(tooltipKey)))
                .build();
    }

    private void applyPreset(final ConfigPreset preset) {
        final Config config = Config.getConfig();
        preset.getApply().accept(config);
        config.HasSelectedPreset = true;
        config.save();

        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int titleY = this.height / 6;
        final Text title = Text.translatable("screen.codeclient.config_preset.title");
        context.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 2, titleY, 0xFFFFFFFF);

        final boolean firstRun = !Config.getConfig().HasSelectedPreset;
        final Text mainSubtitle = firstRun
                ? Text.translatable("screen.codeclient.config_preset.welcome.title")
                : Text.translatable("screen.codeclient.config_preset.subtitle");
        final Text secondarySubtitle = firstRun
                ? Text.translatable("screen.codeclient.config_preset.welcome.subtitle")
                : Text.empty();

        final int textY = titleY + 20;
        context.drawCenteredTextWithShadow(this.textRenderer, mainSubtitle, this.width / 2, textY, 0xFFA0E6FF);

        if (!secondarySubtitle.getString().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, secondarySubtitle, this.width / 2, textY + 12, 0xFFA0E6FF);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.codeclient.config_preset.hover_hint"), this.width / 2, textY + 24, 0xFFCCCCCC);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("screen.codeclient.config_preset.hover_hint"), this.width / 2, textY + 12, 0xFFCCCCCC);
        }

        if (!firstRun) {
            final Text warning = Text.translatable("screen.codeclient.config_preset.warning");
            context.drawCenteredTextWithShadow(this.textRenderer, warning, this.width / 2, this.height / 3, 0xFFFF5555);
        }
    }
}
