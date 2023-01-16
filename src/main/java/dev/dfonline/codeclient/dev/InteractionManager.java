package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.PlotLocation;
import net.minecraft.block.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class InteractionManager {

    public static boolean onBreakBlock(BlockPos pos) {
        if(!PlotLocation.isInCodeSpace(pos)) return false;
        if((pos.getY() + 1) % 5 == 0) return true;
        Item type = CodeClient.MC.world.getBlockState(pos).getBlock().asItem();
        if(List.of(Items.STONE, Items.PISTON, Items.STICKY_PISTON, Items.CHEST).contains(type)) return true;
        if(type == Items.OAK_SIGN) pos = pos.add(1,0,0);
        breakCodeBlock(pos);
        return false;
    }

    public static boolean onPlaceBlock(BlockPos pos) {
        if(PlotLocation.isInCodeSpace(pos)) {
            CodeClient.MC.player.swingHand(Hand.MAIN_HAND);
//            CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(new SoundEvent(new Identifier("minecraft:block.stone.place")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
            return true;
        }
        return false;
    }

    private static void breakCodeBlock(BlockPos pos) {
        ClientWorld world = CodeClient.MC.world;
        CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(new SoundEvent(new Identifier("minecraft:block.stone.break")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.setBlockState(pos.add(0,1,0), Blocks.AIR.getDefaultState());
        world.setBlockState(pos.add(-1,0,0), Blocks.AIR.getDefaultState());
        world.setBlockState(pos.add(0,0,1), Blocks.AIR.getDefaultState());
    }
}
