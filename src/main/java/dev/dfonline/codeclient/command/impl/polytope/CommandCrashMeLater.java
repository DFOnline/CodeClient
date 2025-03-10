package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import javax.swing.*;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandCrashMeLater extends Command {
    @Override
    public String name() {
        return "crashmelater";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer()).executes(context -> {
            Timer timer = new Timer(context.getArgument("milliseconds", Integer.class), arg0 -> System.exit(-69420));
            timer.setRepeats(false); // Only execute once
            timer.start(); // Go go go!


            return 1;
        }));
    }
}
