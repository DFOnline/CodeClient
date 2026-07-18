package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CommandPing extends Command {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            if (CodeClient.MC.player == null) return 0;

            PlayerInfo playerListEntry = CodeClient.MC.player.connection.getPlayerInfo(CodeClient.MC.player.getUUID());

            if (playerListEntry != null) {
                int ping = playerListEntry.getLatency();
                Utility.sendMessage(Component.translatable("codeclient.command.ping", ping), ChatType.SUCCESS);
            }
            return 0;
        });
    }
}
