package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.command.ActionCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CommandConfirmCC extends ActionCommand {
    @Override
    public String name() {
        return "confirmcc";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            if (CodeClient.confirmingAction == null) {
                Utility.sendMessage(Component.translatable("codeclient.action.confirmcc.nothing"), ChatType.INFO);
                return 0;
            }
            if (!(CodeClient.currentAction instanceof None)) {
                Utility.sendMessage(Component.translatable("codeclient.action.busy").append(" ").append(Component.translatable("codeclient.action.abort")), ChatType.FAIL);
                return -1;
            }
            Utility.sendMessage(Component.translatable("codeclient.action.confirmcc.confirm"), ChatType.SUCCESS);
            CodeClient.currentAction = CodeClient.confirmingAction;
            CodeClient.currentAction.init();
            CodeClient.confirmingAction = null;
            return 1;
        });
    }
}
