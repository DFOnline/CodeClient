package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.dev.CodeSearcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandCsexclude extends Command {
    @Override
    public String name() {
        return "csexclude";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(context -> {
            // For versions below 1.19, replace "Text.literal" with "new LiteralText".
            String text = context.getArgument("text", String.class);

            MC.player.sendMessage(Text.literal("Will not match " + text + " until the next /csc"),

                    false);
            CodeSearcher.addExclusion(text);

            return 1;
        }));
    }
}
