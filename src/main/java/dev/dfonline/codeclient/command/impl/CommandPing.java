package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class CommandPing extends Command {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            if (CodeClient.MC.player == null) return 0;

            PlayerListEntry playerListEntry = CodeClient.MC.player.networkHandler.getPlayerListEntry(CodeClient.MC.player.getUuid());

            if (playerListEntry != null) {
                int ping = playerListEntry.getLatency();
                Utility.sendMessage(Text.translatable("codeclient.command.ping", ping), ChatType.SUCCESS);
            }
            return 0;
        });
    }
}
