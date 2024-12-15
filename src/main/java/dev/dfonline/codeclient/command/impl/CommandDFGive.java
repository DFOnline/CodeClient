package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandDFGive extends Command {
    @Override
    public String name() {
        return "dfgive";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .then(argument("count", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    giveItem(ctx.getArgument("item", ItemStackArgument.class)
                                                    .createStack(1, false),
                                            ctx.getArgument("count", Integer.class));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            giveItem(ctx.getArgument("item", ItemStackArgument.class)
                                    .createStack(1, false), 1);
                            return 1;
                        })
                )
                .then(literal("clipboard")
                        .executes(ctx -> {
                            if (CodeClient.MC.getNetworkHandler() == null) return -1;
                            String clipboard;
                            try {
                                clipboard = CodeClient.MC.keyboard.getClipboard();
                            } catch (Exception e) {
                                Utility.sendMessage(Text.translatable("codeclient.command.dfgive.clipboard_unavailable"), ChatType.FAIL);
                                return -1;
                            }

                            // Remove vanilla/dfgive command syntax, only leave the actual item and count arguments.
                            // Every character will match in: '/dfgive @a '
                            clipboard = clipboard.replaceAll("^/?(df)?(give )?(@[parens] )?", "").trim();
                            if (clipboard.isEmpty() || clipboard.equals("clipboard")) {
                                Utility.sendMessage(Text.translatable("codeclient.command.dfgive.clipboard_invalid"), ChatType.FAIL);
                                return -1;
                            }

                            CodeClient.MC.getNetworkHandler().sendCommand("dfgive " + clipboard);
                            return 1;
                        })
                );
    }

    private void giveItem(ItemStack item, int count) {
        if (CodeClient.MC.player == null) return;
        item.setCount(count);

        if (!CodeClient.MC.player.isCreative()) {
            Utility.sendMessage(Text.translatable("codeclient.warning.creative_mode"), ChatType.FAIL);
            return;
        }
        if (count < 1) {
            Utility.sendMessage(Text.translatable("codeclient.command.dfgive.count_below_min"), ChatType.FAIL);
            return;
        }

        if (count <= item.getMaxCount()) {
            CodeClient.MC.player.giveItemStack(item);
            Utility.sendInventory();
        } else {
            Utility.sendMessage(Text.translatable("codeclient.command.dfgive.count_above_max", item.getMaxCount()), ChatType.FAIL);
        }
    }
}
