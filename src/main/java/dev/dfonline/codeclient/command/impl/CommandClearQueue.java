package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.command.CommandSender;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CommandClearQueue extends Command {

    @Override
    public String name() {
        return "ccclearqueue";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            if (CommandSender.queueSize() > 0) {
                CommandSender.clearQueue();
                Utility.sendMessage(Component.translatable("codeclient.action.clearqueue.success"), ChatType.SUCCESS);
            } else {
                Utility.sendMessage(Component.translatable("codeclient.action.clearqueue.empty"), ChatType.FAIL);
            }

            return 1;
        });
    }
}
