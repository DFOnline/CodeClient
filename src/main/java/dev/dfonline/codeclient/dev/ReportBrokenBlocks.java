package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReportBrokenBlocks extends Feature {
    private static final List<Block> VALID_BLOCKS = List.of(
            Blocks.LAPIS_BLOCK,
            Blocks.LAPIS_ORE,
            Blocks.EMERALD_BLOCK,
            Blocks.EMERALD_ORE,
            Blocks.DIAMOND_BLOCK,
            Blocks.GOLD_BLOCK);

    @Override
    public boolean enabled() {
        return Config.getConfig().ReportBrokenBlock;
    }

    @Override
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {
        if (VALID_BLOCKS.contains(CodeClient.MC.world.getBlockState(breakPos).getBlock())
                && CodeClient.MC.world.getBlockEntity(breakPos.west()) instanceof SignBlockEntity sign) {
            Text signText = sign.getFrontText().getMessage(1, false);
            boolean isSignTextEmpty = signText.getString().isEmpty();

            Utility.sendMessage(
                    isSignTextEmpty
                            ? Text.translatable("codeclient.interaction.broke_empty", Text.translatable("hypercube.codeblock." +
                            sign.getFrontText().getMessage(0, false).getString().toLowerCase().replace(' ', '_'))).formatted(Formatting.AQUA)
                            : Text.translatable("codeclient.interaction.broke", Text.empty().formatted(Formatting.WHITE).append(signText)).formatted(Formatting.AQUA)
            );
        }
    }
}
