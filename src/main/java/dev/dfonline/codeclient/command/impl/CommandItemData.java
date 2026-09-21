package dev.dfonline.codeclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.DataResult;
import dev.dfonline.codeclient.ChatType;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.command.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

public class CommandItemData extends Command {
    @Override
    public String name() {
        return "itemdata";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> create(LiteralArgumentBuilder<FabricClientCommandSource> cmd, CommandBuildContext registryAccess) {
        return cmd.executes(context -> {
            LocalPlayer player = CodeClient.MC.player;
            if (player == null) return -1;
            ItemStack item = player.getInventory().getSelectedItem();
            if (CodeClient.MC.level == null) return -1;
            Optional<Tag> nbt = ItemStack.CODEC.encodeStart(CodeClient.MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), item).result();

            if (nbt.isEmpty()) {
                Utility.sendMessage(Component.translatable("codeclient.command.itemdata.no_nbt"), ChatType.FAIL);
                return 0;
            }

            player.sendSystemMessage(
                    Component.literal(" ".repeat(15)).setStyle(Style.EMPTY.withStrikethrough(true).withColor(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(" ").setStyle(Style.EMPTY.withStrikethrough(false).withColor(ChatFormatting.AQUA))
                                    .append(Component.translatable("codeclient.command.itemdata.header", Component.empty().withStyle(ChatFormatting.WHITE).append(item.getHoverName())))
                                    .append(Component.literal(" ")))
                            .append(" ".repeat(15))
            );
            player.sendSystemMessage(Component.literal(nbt.get().toString()));

            String unformatted = nbt.get().toString();

            Optional<ResourceKey<Item>> itemKey = BuiltInRegistries.ITEM.getResourceKey(item.getItem());
            Utility.sendMessage(
                    Component.translatable("codeclient.command.itemdata.copy",
                            Component.translatable("codeclient.command.itemdata.copy.unformatted")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("codeclient.hover.click_to_copy")))
                                            .withClickEvent(new ClickEvent.CopyToClipboard(unformatted))
                                    ),

                            Component.translatable("codeclient.command.itemdata.copy.dfgive")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("codeclient.hover.click_to_copy")))
                                            .withClickEvent(new ClickEvent.CopyToClipboard(
                                                    "/dfgive " + (itemKey.isEmpty() ? "unknown" : itemKey.get().identifier()) + unformatted + " 1")
                                            )
                                    )
                    ), ChatType.SUCCESS);
            return 0;
        });
    }
}
