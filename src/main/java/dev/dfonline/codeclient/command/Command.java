package dev.dfonline.codeclient.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public abstract class Command {

    protected static final Component DONE = Component.translatable("gui.done");

    public String[] aliases() {
        return new String[0];
    }

    public abstract String name();

    public abstract LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess);

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(create(literal(name()), registryAccess));

        for (var alias : aliases()) {
            dispatcher.register(create(literal(alias), registryAccess));
        }
    }
}
