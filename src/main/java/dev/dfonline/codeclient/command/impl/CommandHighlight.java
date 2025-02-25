package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.dev.SignOutlineRenderer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandHighlight extends Command {

    @Override
    public String name() {
        return "highlight";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("x", integer()).then(argument("y", integer()).then(argument("z", integer()).executes(context -> {
            var x = context.getArgument("x", Integer.class);
            var y = context.getArgument("y", Integer.class);
            var z = context.getArgument("z", Integer.class);

            SignOutlineRenderer.highlightedSign = new BlockPos(x, y, z);
            return 0;
        }))))
                .then(literal("clear").executes(context -> {
                    SignOutlineRenderer.highlightedSign = null;
                    return 0;
                }));
    }

}
