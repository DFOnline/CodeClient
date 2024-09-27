package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.impl.PlaceTemplates;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandTemplatePlacer extends TemplateActionCommand {
    @Override
    public String name() {
        return "templateplacer";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes(context -> {
            if (CodeClient.location instanceof Dev) {
                var action = PlaceTemplates.createPlacer(Utility.templatesInInventory(), this::actionCallback);
                if (action == null) return -1;
                CodeClient.currentAction = action;
                CodeClient.currentAction.init();
                return 0;
            }
            Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
            return -1;
        }).then(argument("path", greedyString()).suggests(this::suggestTemplates).executes(context -> {
            try {
                if (CodeClient.location instanceof Dev dev) {
                    var map = new HashMap<BlockPos, ItemStack>();
                    BlockPos pos = dev.findFreePlacePos();
                    for (var template : Objects.requireNonNull(getAllTemplates(FileManager.templatesPath().resolve(context.getArgument("path", String.class))))) {
                        map.put(pos, Utility.makeTemplate(template));
                        if (pos == null) continue;
                        pos = dev.findFreePlacePos(pos.west(2));
                    }
                    CodeClient.currentAction = new PlaceTemplates(map, this::actionCallback);
                    return 0;
                }
                Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                return -1;
            } catch (Exception e) {
                Utility.sendMessage(Text.translatable("codeclient.files.template_fail"), ChatType.FAIL);
                return -2;
            }
        }));
    }
}
