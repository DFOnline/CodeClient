package dev.dfonline.codeclient.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.dfonline.codeclient.command.impl.*;
import dev.dfonline.codeclient.command.impl.polytope.*;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.List;

public class CommandManager {
    private static final List<Command> COMMANDS = List.of(
            new CommandAbort(),
            new CommandAuth(),
            new CommandCalc(),
            new CommandCCConfig(),
            new CommandConfirmCC(),
            new CommandDelete(),
            new CommandDFGive(),
            new CommandFixCC(),
            new CommandGetActionDump(),
            new CommandGetSize(),
            new CommandGetSpawn(),
            new CommandItemData(),
            new CommandJump(),
            new CommandJumpToFreeSpot(),
            new CommandLoad(),
            new CommandName(),
            new CommandNode(),
            new CommandPing(),
            new CommandPreview(),
            new CommandSave(),
            new CommandScanFor(),
            new CommandScanPlot(),
            new CommandSearch(),
            new CommandSwap(),
            new CommandTemplatePlacer(),
            new CommandUuid(),
            new CommandWidthDump(),
            new CommandWorldPlot(),
            new CommandClearQueue(),

            /* Polytope */
            new CommandBubbles(),
            new CommandCrashMeLater(),
            new CommandCrashMeNow(),
            new CommandCs(),
            new CommandCsc(),
            new CommandCsexclude(),
            new CommandCsl(),
            new CommandCst(),
            new CommandGo(),
            new CommandHome(),
            new CommandRainbowify(),
            new CommandSmcaps(),
            new CommandBackwarp()
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
