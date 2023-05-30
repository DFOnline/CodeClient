package dev.dfonline.codeclient.dev;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InteractionManager {

    public static boolean onBreakBlock(BlockPos pos) {
        if(CodeClient.location instanceof Dev plot) {
            if (!plot.isInCodeSpace(pos.getX(), pos.getZ())) return false;
            if ((pos.getY() + 1) % 5 == 0) return true;
            Block type = CodeClient.MC.world.getBlockState(pos).getBlock();
            if (List.of(Blocks.STONE, Blocks.DIRT, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.CHEST).contains(type)) return true;
            if (type == Blocks.OAK_SIGN) pos = pos.add(1, 0, 0);
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
                    if (!varItem.asString().startsWith("{\"id\":\"bl_tag\",\"data\":") || !Config.getConfig().CustomTagInteraction) return false;
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

    public static boolean isInsideWall(Vec3d playerPos) {
        Vec3d middlePos = playerPos.add(0,0.75,0);
        Box box = new EntityDimensions(0.6f,1.8f,true).getBoxAt(playerPos); //Box.of(middlePos, 0.6, 5, 0.6);
        CodeClient.LOGGER.info(String.valueOf(box));
        return BlockPos.stream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.world.getBlockState(pos);
            return !blockState.isAir() && VoxelShapes.matchesAnywhere(blockState.getCollisionShape(CodeClient.MC.world, pos).offset(pos.getX(), pos.getY(), pos.getZ()), VoxelShapes.cuboid(box), BooleanBiFunction.AND);
        });
    }

    public static BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        if(CodeClient.location instanceof Dev plot) {
            BlockPos pos = hitResult.getBlockPos();
            if(plot.isInCodeSpace(pos.getX(), plot.getZ()) && pos.getY() % 5 == 4) { // Is a code space level (glass)
                if(hitResult.getSide() == Direction.UP || hitResult.getSide() == Direction.DOWN) {
                    if(CodeClient.MC.world.getBlockState(pos).isAir() && !Config.getConfig().PlaceOnAir) return hitResult;
                    BlockHitResult newHitResult = new BlockHitResult(hitResult.getPos(),Direction.DOWN,hitResult.getBlockPos().add(0,1,0),hitResult.isInsideBlock());
                    return newHitResult;
                }
            }
            if(hitResult.getSide() == Direction.DOWN) {
                BlockHitResult newHitResult = new BlockHitResult(hitResult.getPos(),Direction.UP,hitResult.getBlockPos(), hitResult.isInsideBlock());
                return newHitResult;
            }
        }
        return hitResult;
    }
//    public static boolean onBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
//        MinecraftClient MC = CodeClient.MC;
//        if(CodeClient.location instanceof Dev plot && plot.isInCodeSpace(hitResult.getPos().getX(), hitResult.getPos().getZ())) {
//            Vec3d origin = player.getEyePos();
//            if(origin.distanceTo(hitResult.getPos()) > 5.4) {
//                Vec3d diff = hitResult.getPos().relativize(origin);
//                Vec3d pos = hitResult.getPos().add(diff.normalize().multiply(5.4));
//
//                boolean collides = isInsideWall(pos);
//                CodeClient.LOGGER.info(String.valueOf(collides));
//                if(collides) {
//                    Direction[] directions = new Direction[]{Direction.UP, Direction.WEST};
//                    for (Direction direction: directions) {
//                        BlockPos offset = BlockPos.ofFloored(pos.offset(direction,1));
//                        if(!isInsideWall(offset.toCenterPos())) {
//                            CodeClient.LOGGER.info(String.valueOf(direction));
//                            pos = offset.toCenterPos();
//                            break;
//                        }
//                    }
//                }
//
//                Vec3d eyeRel = origin.relativize(player.getPos());
//                MoveToLocation.shove(player,pos.add(eyeRel));
//            }
//
////            hitResult;
//            ItemStack item = player.getStackInHand(hand);
//            boolean clickedStone = MC.world.getBlockState(hitResult.getBlockPos()).getBlock().equals(Blocks.STONE);
//            boolean onTop = hitResult.getSide() == Direction.UP && ((hitResult.getBlockPos().getY() + 1) % 5 == 0);
//            boolean isTemplate = item.hasNbt() && item.getNbt() != null && item.getNbt().contains("PublicBukkitValues", NbtElement.COMPOUND_TYPE) && item.getNbt().getCompound("PublicBukkitValues").contains("hypercube:codetemplatedata", NbtElement.STRING_TYPE);
//
//            if(onTop && Config.getConfig().PlaceOnAir) {
//                BlockPos from = hitResult.getBlockPos();
//                hitResult = new BlockHitResult(new Vec3d(from.getX(), from.getY() + 1, from.getZ()),Direction.UP,hitResult.getBlockPos().add(0,1,0),false);
//            }
//
//
//            BlockHitResult finalHitResult = hitResult;
//            if(onTop || isTemplate || Config.getConfig().CustomBlockInteractions) {
//                // play sound
//                BlockSoundGroup group = Block.getBlockFromItem(item.getItem()).getSoundGroup(Block.getBlockFromItem(item.getItem()).getDefaultState());
//                MC.getSoundManager().play(new PositionedSoundInstance(group.getPlaceSound(), SoundCategory.BLOCKS, group.getVolume(), group.getPitch(), Random.create(), hitResult.getBlockPos()));
//
//                MC.player.swingHand(Hand.MAIN_HAND);
//
//                if(isTemplate) {
//                    ItemStack template = Items.ENDER_CHEST.getDefaultStack();
//                    template.setNbt(item.getNbt());
//                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + player.getInventory().selectedSlot, template));
//                }
//                ((ClientPlayerInteractionManagerAccessor) (CodeClient.MC.interactionManager)).invokeSequencedPacket(CodeClient.MC.world, sequence -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, finalHitResult, sequence));
//                CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + player.getInventory().selectedSlot, item));
//                return true;
//            }
//        }
//        return false;
//    }

    @Nullable
    public static VoxelShape customVoxelShape(BlockView world, BlockPos pos) {
        if(CodeClient.location instanceof Dev plot) {
            Config.LayerInteractionMode mode = Config.getConfig().CodeLayerInteractionMode;
            boolean isLevel = plot.isInCodeSpace(pos.getX(), plot.getZ()) && pos.getY() % 5 == 4;
            boolean noClipAllowsBlock = Config.getConfig().NoClipEnabled || world.getBlockState(pos).isAir();
            boolean hideCodeSpace =
                    noClipAllowsBlock &&
                    mode != Config.LayerInteractionMode.OFF
                    && isLevel
                    && (
                            mode == Config.LayerInteractionMode.ON
                            || pos.getY() + 1 < CodeClient.MC.player.getEyeY()
                    )
                    && !world.getBlockState(pos.add(0,1,0)).isSolidBlock(world, pos)
            ;
            if(hideCodeSpace) return VoxelShapes.cuboid(0, 1 - 1d / (4096) ,0,1,1,1);
            if(noClipAllowsBlock && mode != Config.LayerInteractionMode.OFF && pos.getY() + 1 > CodeClient.MC.player.getEyeY() && isLevel) return VoxelShapes.empty();
        }
        return null;
    }
}
