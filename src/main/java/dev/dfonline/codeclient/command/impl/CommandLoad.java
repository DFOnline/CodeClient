package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.TemplateActionCommand;
import dev.dfonline.codeclient.data.DFItem;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandLoad extends TemplateActionCommand {
    @Override
    public String name() {
        return "load";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.then(argument("path", greedyString()).suggests(this::suggestTemplates).executes(context -> {
            if (CodeClient.MC.player == null) return -1;
            if (CodeClient.MC.player.isCreative()) {
                String arg = context.getArgument("path", String.class);
                Path path = FileManager.templatesPath().resolve(arg + ".dft");
                if (Files.notExists(path)) {
                    Utility.sendMessage(Component.translatable("codeclient.files.error.cant_read"), ChatType.FAIL);
                    return -1;
                }
                try {
                    byte[] data = Files.readAllBytes(path);
                    ItemStack template = Utility.makeTemplate(new String(Base64.getEncoder().encode(data)));
                    DFItem dfItem = new DFItem(template);
                    dfItem.setName(Component.empty().withStyle(ChatFormatting.RED).append("Saved Template").append(Component.literal(" » ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)).append(String.valueOf(FileManager.templatesPath().relativize(path))));
                    CodeClient.MC.player.addItem(dfItem.getItemStack());
                    Utility.sendInventory();
                } catch (Exception e) {
                    Utility.sendMessage(Component.translatable("codeclient.files.error.read_file", path), ChatType.FAIL);
                    return -2;
                }
                return 0;
            }
            Utility.sendMessage(Component.translatable("codeclient.warning.creative_mode"), ChatType.FAIL);
            return -1;
        }));
    }
}
