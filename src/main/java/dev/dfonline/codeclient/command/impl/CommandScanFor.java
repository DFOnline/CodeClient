package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.ActionCommand;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CommandScanFor extends ActionCommand {
    @Override
    public String name() {
        return "scanfor";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd) {
        return cmd.then(argument("name", greedyString()).executes(context -> {
            if (CodeClient.location instanceof Dev dev) {
                Pattern pattern = Pattern.compile(context.getArgument("name", String.class), Pattern.CASE_INSENSITIVE);
                var scan = dev.scanForSigns(pattern);
                Utility.sendMessage(Text.translatable("codeclient.action.scanfor.scan_result"));
                for (var res : scan.entrySet()) {
                    Utility.sendMessage(Text.empty().append("- ").append(res.getKey().toString()).append(": ").append(res.getValue().getMessage(1, false)));
                }
                return 0;
            }
            Utility.sendMessage(Text.translatable("codeclient.action.scanfor.scan_fail"), ChatType.FAIL);
            return -1;
        }));
    }
}
