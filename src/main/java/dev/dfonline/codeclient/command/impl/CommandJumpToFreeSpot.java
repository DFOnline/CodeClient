package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.GoTo;
import dev.dfonline.codeclient.command.ActionCommand;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.math.BlockPos;

public class CommandJumpToFreeSpot extends ActionCommand {
    @Override
    public String name() {
        return "jumptofreespot";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes(context -> {
            if (CodeClient.MC.player == null) return -1;
            if (CodeClient.location instanceof Dev dev) {
                BlockPos freePlacePos = dev.findFreePlacePos(CodeClient.MC.player.getBlockPos());
                if (freePlacePos == null) return -1;
                CodeClient.currentAction = new GoTo(freePlacePos.toCenterPos().add(0, -0.5, 0), this::actionCallback);
                CodeClient.currentAction.init();
                return 0;
            }
            return -2;
        });
    }
}
