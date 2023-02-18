package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.PlotLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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

    public static boolean onClickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {
        if(!slot.hasStack()) return false;
        ItemStack item = slot.getStack();
        if(!item.hasNbt()) return false;
        NbtCompound nbt = item.getNbt();
        if(!nbt.contains("PublicBukkitValues")) return false;
        if(nbt.get("PublicBukkitValues") instanceof NbtCompound bukkitValues) {
            if(!bukkitValues.contains("hypercube:varitem")) return false;
            if(bukkitValues.get("hypercube:varitem") instanceof NbtString varItem) {
                if(!varItem.asString().startsWith("{\"id\":\"bl_tag\",\"data\":")) return false;
                Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap();
                CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(syncId,revision,slot.getIndex(),button,actionType,item,int2ObjectMap));
                return true;
            }
        }
        return false;
    }
}
