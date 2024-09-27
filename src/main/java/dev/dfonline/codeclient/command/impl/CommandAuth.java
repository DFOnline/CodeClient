package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandAuth extends Command {
    @Override
    public String name() {
        return "auth";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes(context -> {
            CodeClient.API.setAcceptedScopes(true);
            Utility.sendMessage(Text.translatable("codeclient.api.authorised")
                            .append(Text.literal("\n")).append(Text.translatable("codeclient.api.remove"))
                    , ChatType.SUCCESS);
            return 0;
        }).then(literal("remove").executes(context -> {
            CodeClient.API.setAcceptedScopes(false);
            Utility.sendMessage(Text.translatable("codeclient.api.removed"), ChatType.SUCCESS);
            return 0;
        })).then(literal("disconnect").executes(context -> {
            Utility.sendMessage(Text.translatable("codeclient.api.disconnected"), ChatType.SUCCESS);
            CodeClient.API.setConnection(null);
            return 0;
        }));
    }
}
