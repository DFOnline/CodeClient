package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandSwap extends TemplateActionCommand {
    @Override
    public String name() {
        return "swap";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.then(argument("path", greedyString()).suggests(this::suggestTemplates).executes(context -> {
            Path path = FileManager.templatesPath().resolve(context.getArgument("path", String.class));
            try {
                ArrayList<ItemStack> map = new ArrayList<>();
                for (var template : Objects.requireNonNull(getAllTemplates(path))) {
                    map.add(Utility.makeTemplate(template));
                }
                PlaceTemplates swapper = PlaceTemplates.createSwapper(map, this::actionCallback);
                if (swapper != null) {
                    CodeClient.confirmingAction = swapper.swap();
                    Utility.sendMessage(Text.translatable("codeclient.action.confirmcc.use"), ChatType.INFO);
                }
            } catch (IOException e) {
                Utility.sendMessage(Text.translatable("codeclient.files.error.read_file", path), ChatType.FAIL);
            }
            return 0;
        }));
    }
}
