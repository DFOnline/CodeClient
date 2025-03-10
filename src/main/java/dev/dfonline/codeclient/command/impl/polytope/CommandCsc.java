package dev.dfonline.codeclient.command.impl.polytope;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.command.Command;
import dev.dfonline.codeclient.dev.CodeSearcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static dev.dfonline.codeclient.CodeClient.MC;

public class CommandCsc extends Command {
    @Override
    public String name() {
        return "csc";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes((context -> {
            assert MC.player != null;
            MC.player.sendMessage(Text.literal("Cleared your search!"), false);
            CodeSearcher.setSearchString("", 0);
            return 1;
        }));
    }
}
