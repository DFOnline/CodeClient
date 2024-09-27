package dev.dfonline.codeclient.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.dfonline.codeclient.command.impl.*;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.List;

public class CommandManager {
    private static final List<Command> COMMANDS = List.of(
            new CommandAbort(),
            new CommandAuth(),
            new CommandCCConfig(),
            new CommandConfirmCC(),
            new CommandDelete(),
            new CommandDFGive(),
            new CommandFixCC(),
            new CommandGetActionDump(),
            new CommandGetSize(),
            new CommandGetSpawn(),
            new CommandJump(),
            new CommandJumpToFreeSpot(),
            new CommandLoad(),
            new CommandPing(),
            new CommandSave(),
            new CommandScanFor(),
            new CommandScanPlot(),
            new CommandSearch(),
            new CommandSwap(),
            new CommandTemplatePlacer(),
            new CommandWidthDump(),
            new CommandWorldPlot()
    );

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        COMMANDS.forEach(command -> {
            if (command.name() == null || command.name().isEmpty() || command.name().contains(" ")) {
                throw new IllegalStateException("Command name cannot be null, empty, or include whitespace. At class: " + command.getClass().getSimpleName());
            }
            command.register(dispatcher, registryAccess);
        });
    }
}
