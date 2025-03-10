package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandGo extends Command {
    @Override
    public String name() {
        return "go";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("where", IntegerArgumentType.integer(-9, 40))

                .executes((context -> {
                            if (!(CodeClient.location instanceof Dev)) {
                                context.getSource().sendError(Text.literal("You can only execute this command while in dev mode!"));
                                return 0;
                            }

                            int y = (5 * context.getArgument("where", Integer.class)) + 50;

                            MC.player.networkHandler.sendChatCommand("plot tp -19 " + y + " 10");

                            return 1;
                        })
                )
        );
    }
}
