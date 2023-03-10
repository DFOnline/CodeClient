package dev.dfonline.codeclient.dev;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.mixin.ClientPlayerInteractionManagerAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class InteractionManager {

    public static boolean onBreakBlock(BlockPos pos) {
        if(CodeClient.location instanceof Dev plot) {
            if (!plot.isInCodeSpace(pos.getX(), pos.getZ())) return false;
            if ((pos.getY() + 1) % 5 == 0) return true;
            Item type = CodeClient.MC.world.getBlockState(pos).getBlock().asItem();
            if (List.of(Items.STONE, Items.PISTON, Items.STICKY_PISTON, Items.CHEST).contains(type)) return true;
            if (type == Items.OAK_SIGN) pos = pos.add(1, 0, 0);
            breakCodeBlock(pos);
            return true;
        }
        return false;
    }

    public static boolean onPlaceBlock(BlockPos pos) {
        if(CodeClient.location instanceof Dev plot) {
            if(plot.isInCodeSpace(pos.getX(), pos.getZ())) {
                CodeClient.MC.player.swingHand(Hand.MAIN_HAND);
    //            CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(new SoundEvent(new Identifier("minecraft:block.stone.place")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
                return true;
            }
            return false;
        }
        return true;
    }

    private static void breakCodeBlock(BlockPos pos) {
        ClientWorld world = CodeClient.MC.world;
        CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(SoundEvent.of(new Identifier("minecraft:block.stone.break")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
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
            try {
                if (bukkitValues.get("hypercube:varitem") instanceof NbtString varItem) {
                    if (!varItem.asString().startsWith("{\"id\":\"bl_tag\",\"data\":")) return false;
                    Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap();
                    CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(syncId, revision, slot.getIndex(), button, SlotActionType.PICKUP, item, int2ObjectMap));
                    NbtList lore = (NbtList) nbt.getCompound("display").get("Lore");
                    int offset = 1;
                    int currentIndex = 0;
                    int i = -1;
                    for (NbtElement nbtElement : lore) {
                        i++;
                        JsonElement element = JsonParser.parseString(nbtElement.asString());
                        if (!element.isJsonObject()) continue;
                        JsonObject json = element.getAsJsonObject();

                        if (nbtElement.asString().equals("{\"text\":\"\"}")) {
                            offset = i + 1;
                        }

                        if (json.has("extra") && json.get("extra").isJsonArray() && json.get("extra").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().equals("» ")) {
                            lore.set(i, NbtString.of("{\"text\":\"\",\"extra\":[{\"text\":\"" + json.get("extra").getAsJsonArray().get(1).getAsJsonObject().get("text").getAsString() + "\",\"color\":\"#808080\"}]}"));
                            currentIndex = i;
                            break;
                        }
                    }
                    int nextline = (currentIndex + (button == 1 ? -2 : 0) + 1);
                    if(nextline == lore.size()) nextline = offset;
                    if (nextline == offset - 1) nextline = lore.size() - 1;
                    if (nextline != -1) {
                        String name = JsonParser.parseString(lore.get(nextline).asString()).getAsJsonObject().get("extra").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                        lore.set(nextline, NbtString.of("{\"text\":\"\",\"extra\":[{\"color\":\"dark_aqua\",\"text\":\"» \"},{\"color\":\"aqua\",\"text\":\"" + name + "\"}]}"));
                    }
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean onBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if(CodeClient.location instanceof Dev plot)
        if(plot.isInCodeSpace(hitResult.getPos().getX(), hitResult.getPos().getZ())) {
            CodeClient.MC.player.swingHand(Hand.MAIN_HAND);
//            CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(new SoundEvent(new Identifier("minecraft:block.stone.place")), SoundCategory.BLOCKS, 2, 0.8F, Random.create(), pos));
            if(hitResult.getSide() == Direction.UP) {
                BlockPos from = hitResult.getBlockPos();
                hitResult = new BlockHitResult(new Vec3d(from.getX(), from.getY() + 1, from.getZ()),Direction.UP,hitResult.getBlockPos().add(0,1,0),false);
            }
            BlockHitResult finalHitResult = hitResult;
            ((ClientPlayerInteractionManagerAccessor) (CodeClient.MC.interactionManager)).invokeSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, finalHitResult, sequence));
            return true;
        }
        return false;
    }
}
