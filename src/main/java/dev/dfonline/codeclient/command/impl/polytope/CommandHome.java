package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandHome extends Command {
    @Override
    public String name() {
        return "home";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes((context -> {
                    if (!(CodeClient.location instanceof Dev)) {
                        Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                        return 0;
                    }

                    MC.player.networkHandler.sendChatCommand("plot tp 0 50 0");

                    return 1;
                })
        );
    }
}
