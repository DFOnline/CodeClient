package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.command.impl.CommandJump;
import dev.dfonline.codeclient.dev.Backwarp;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandBackwarp extends Command {
    @Override
    public String name() {
        return "backwarp";
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        super.register(dispatcher, registryAccess);
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.then(argument("query", greedyString()).suggests((context, builder) -> CommandJump.suggestJump(CommandJump.JumpType.ANY, context, builder)).executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                    var query = context.getArgument("query", String.class);

                    Backwarp.BackwarpResult result = Backwarp.to(dev, "CALL FUNCTION", query);

                    if (result.message != null) {
                        if (result.success) {
                            Utility.sendMessage(result.message, ChatType.SUCCESS);
                        } else {
                            result = Backwarp.to(dev, "START PROCESS", query);
                            if (result.message != null) {
                                if (result.success) {
                                    Utility.sendMessage(result.message, ChatType.SUCCESS);
                                } else {
                                    Utility.sendMessage(result.message, ChatType.INFO);

                                }

                            }
                        }

                    }


                } else {
                    Utility.sendMessage(Text.translatable("codeclient.warning.dev_mode"), ChatType.FAIL);
                }
            return 0;
        }));
    }
}
