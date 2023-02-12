package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.PlotLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class InteractionManager {

    public static boolean onBreakBlock(BlockPos pos) {
        if(!PlotLocation.isInCodeSpace(pos)) return false;
        if((pos.getY() + 1) % 5 == 0) return true;
        Item type = CodeClient.MC.level.getBlockState(pos).getBlock().asItem();
        if(List.of(Items.STONE, Items.PISTON, Items.STICKY_PISTON, Items.CHEST).contains(type)) return true;
        if(type == Items.OAK_SIGN) pos = pos.offset(1,0,0);
        breakCodeBlock(pos);
        return false;
    }

    public static boolean onPlaceBlock(BlockPos pos) {
        if(PlotLocation.isInCodeSpace(pos)) {
            CodeClient.MC.player.swing(InteractionHand.MAIN_HAND);
//            CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(new SoundEvent(new Identifier("minecraft:block.stone.place")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
            return true;
        }
        return false;
    }

    private static void breakCodeBlock(BlockPos pos) {
        ClientLevel world = CodeClient.MC.level;
        CodeClient.MC.getSoundManager().play(new SimpleSoundInstance(new SoundEvent(new ResourceLocation("minecraft:block.stone.break")), SoundSource.BLOCKS, 2, 0.8F, RandomSource.create(), pos));
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        world.setBlockAndUpdate(pos.offset(0,1,0), Blocks.AIR.defaultBlockState());
        world.setBlockAndUpdate(pos.offset(-1,0,0), Blocks.AIR.defaultBlockState());
        world.setBlockAndUpdate(pos.offset(0,0,1), Blocks.AIR.defaultBlockState());
    }
}
