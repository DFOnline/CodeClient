package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.action.impl.GoTo;
import dev.dfonline.codeclient.command.ActionCommand;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class CommandJumpToFreeSpot extends ActionCommand {
    @Override
    public String name() {
        return "jumptofreespot";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            if (CodeClient.MC.player == null) return -1;
            if (CodeClient.location instanceof Dev dev) {
                BlockPos freePlacePos = dev.findFreePlacePos(CodeClient.MC.player.blockPosition());
                if (freePlacePos == null) return -1;
                CodeClient.currentAction = new GoTo(Vec3.atBottomCenterOf(freePlacePos), this::actionCallback);
                CodeClient.currentAction.init();
                return 0;
            }
            return -2;
        });
    }
}
