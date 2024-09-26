package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.MoveToSpawn;
import dev.dfonline.codeclient.command.ActionCommand;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class CommandGetSpawn extends ActionCommand {
    @Override
    public String name() {
        return "getspawn";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            if (!(CodeClient.location instanceof Dev)) return 1;
            CodeClient.currentAction = new MoveToSpawn(this::actionCallback);
            CodeClient.currentAction.init();
            return 0;
        });
    }
}
