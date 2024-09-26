package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.None;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.dev.debug.Debug;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class CommandAbort extends Command {
    @Override
    public String name() {
        return "abort";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            CodeClient.confirmingAction = null;
            CodeClient.currentAction = new None();
            CodeClient.getFeature(Debug.class).ifPresent(Debug::reset);
            CodeClient.API.abort();
            return 0;
        });
    }
}
