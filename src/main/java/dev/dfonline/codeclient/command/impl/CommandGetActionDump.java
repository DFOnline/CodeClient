package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.action.impl.GetActionDump;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandGetActionDump extends Command {
    @Override
    public String name() {
        return "getactiondump";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.NONE, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        }).then(literal("section").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.SECTION, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        })).then(literal("ampersand").executes(context -> {
            CodeClient.currentAction = new GetActionDump(GetActionDump.ColorMode.AMPERSAND, () -> Utility.sendMessage(DONE, ChatType.SUCCESS));
            CodeClient.currentAction.init();
            return 0;
        }));
    }
}
