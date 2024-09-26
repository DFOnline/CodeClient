package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class CommandFixCC extends Command {
    @Override
    public String name() {
        return "fixcc";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.executes(context -> {
            CodeClient.reset();
            return 0;
        });
    }
}
