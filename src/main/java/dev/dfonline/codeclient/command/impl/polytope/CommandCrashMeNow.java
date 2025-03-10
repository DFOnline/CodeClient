package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandCrashMeNow extends Command {
    @Override
    public String name() {
        return "crashmenow";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes((context -> {
            assert MC.player != null;
            // bye bye!
            System.exit(-69420);
            return 1;
        }));
    }
}
