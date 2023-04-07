package dev.dfonline.codeclient.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.TickBoxController;
import net.minecraft.text.Text;

public class Config {
    public final YetAnotherConfigLib config;
    public boolean NoClipEnabled;

    public static Config getConfig() {
        try {
            return CodeClient.gson.fromJson(FileManager.readFile("options.json"), Config.class);
        }
        catch (Exception ignored) {
            return new Config();
        }
    }

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

}
