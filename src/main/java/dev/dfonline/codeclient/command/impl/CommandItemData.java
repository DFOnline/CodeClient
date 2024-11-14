package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;

public class CommandItemData extends Command {
    @Override
    public String name() {
        return "itemdata";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandRegistryAccess registryAccess) {
        return cmd.executes(context -> {
            ClientPlayerEntity player = CodeClient.MC.player;
            if (player == null) return -1;
            ItemStack item = player.getInventory().getMainHandStack();
            if (CodeClient.MC.world == null) return -1;
            NbtElement nbt = item.toNbt(CodeClient.MC.world.getRegistryManager());

            if (nbt == null) {
                Utility.sendMessage(Text.translatable("codeclient.command.itemdata.no_nbt"), ChatType.FAIL);
                return 0;
            }

            player.sendMessage(
                    Text.literal(" ".repeat(15)).setStyle(Style.EMPTY.withStrikethrough(true).withColor(Formatting.DARK_GRAY))
                            .append(Text.literal(" ").setStyle(Style.EMPTY.withStrikethrough(false).withColor(Formatting.AQUA))
                                    .append(Text.translatable("codeclient.command.itemdata.header", Text.empty().formatted(Formatting.WHITE).append(item.getName())))
                                    .append(Text.literal(" ")))
                            .append(" ".repeat(15)), false
            );
            player.sendMessage(Text.literal(nbt.toString()), false);

            String unformatted = nbt.toString();

            Optional<RegistryKey<Item>> itemKey = Registries.ITEM.getKey(item.getItem());
            Utility.sendMessage(
                    Text.translatable("codeclient.command.itemdata.copy",
                            Text.translatable("codeclient.command.itemdata.copy.unformatted")
                                    .setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.hover.click_to_copy")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, unformatted))
                                    ),

                            Text.translatable("codeclient.command.itemdata.copy.dfgive")
                                    .setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("codeclient.hover.click_to_copy")))
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                    "/dfgive " + (itemKey.isEmpty() ? "unknown" : itemKey.get().getValue()) + unformatted + " 1")
                                            )
                                    )
                    ), ChatType.SUCCESS);
            return 0;
        });
    }
}
