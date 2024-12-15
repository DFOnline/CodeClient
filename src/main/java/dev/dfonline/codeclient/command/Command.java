package dev.dfonline.codeclient.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public abstract class Command {

    protected static final Text DONE = Text.translatable("gui.done");

    public String[] aliases() {
        return new String[0];
    }

    public abstract String name();

    public abstract LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess);

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(create(literal(name()), registryAccess));

        for (var alias : aliases()) {
            dispatcher.register(create(literal(alias), registryAccess));
        }
    }
}
