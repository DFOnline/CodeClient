package dev.dfonline.codeclient;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import net.minecraft.text.Text;

public class Config implements ModMenuApi {
    private final YetAnotherConfigLib config;
    public boolean NoClipEnabled;

    public Config() {
            config = YetAnotherConfigLib.createBuilder()
                    .title(Text.literal("CodeClient Config"))
                    .category(ConfigCategory.createBuilder()
                        .name(Text.literal("CodeClient"))
                        .tooltip(Text.literal("Enable or disable features of CodeClient"))
                        .option(Option.createBuilder(boolean.class)
                                .name(Text.literal("NoClip"))
                                .tooltip(Text.literal("If you can NoClip in the dev space"))
                                .binding(
                                        true,
                                        () -> this.NoClipEnabled,
                                        option -> this.NoClipEnabled = option
                                )
                                .controller(TickBoxController::new)
                                .build()
                        ).build())
                    .build();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new Config().config.generateScreen(parent);
    }
}
