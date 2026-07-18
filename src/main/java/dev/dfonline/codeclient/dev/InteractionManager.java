package dev.dfonline.codeclient.dev;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.switcher.ScopeSwitcher;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InteractionManager {

    public static boolean isOpeningCodeChest = false;

    /**
     * Assuming pos is in a codespace.
     *
     * @param pos The targeted pos.
     * @return The origin of the codeblock to be broken. null if it shouldn't break.
     */
    public static BlockPos isBlockBreakable(BlockPos pos) {
        if (pos.getY() % 5 != 0) return null;
        Block type = CodeClient.MC.level.getBlockState(pos).getBlock();
        if (List.of(Blocks.STONE, Blocks.DIRT, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.CHEST).contains(type))
            return null;
        if (type == Blocks.OAK_WALL_SIGN) return pos.east();
        return pos;
    }

    /**
     * Get the origin of the given codeblock. Assuming you are in the codespace.
     *
     * @param pos The targeted pos.
     * @return The origin of the codeblock. null if there is none.
     */
    public static BlockPos targetedBlockPos(BlockPos pos) {
        Block type = CodeClient.MC.level.getBlockState(pos).getBlock();
        if (List.of(Blocks.DIRT, Blocks.AIR).contains(type))
            return null;
        if (type == Blocks.OAK_WALL_SIGN) return pos.east();
        if (type == Blocks.CHEST) return pos.below();
        if (type == Blocks.STONE || type == Blocks.PISTON || type == Blocks.STICKY_PISTON) {
            var pos2 = pos.north();
            if (List.of(Blocks.STONE, Blocks.DIRT, Blocks.PISTON, Blocks.STICKY_PISTON).contains(CodeClient.MC.level.getBlockState(pos2).getBlock()))
                return null;
            return pos2;
        }
        if (List.of(Blocks.PISTON, Blocks.STICKY_PISTON).contains(type))
            return null;
        return pos;
    }

    public static boolean onBreakBlock(BlockPos pos) {
        if (CodeClient.location instanceof Dev dev) {
            if (!dev.isInDev(pos)) return false;
            BlockPos breakPos = isBlockBreakable(pos);
            CodeClient.onBreakBlock(dev,pos,breakPos);
            if (breakPos != null) {
                breakCodeBlock(breakPos);
            }
            return true;
        }
        return false;
    }

    private static void breakCodeBlock(BlockPos pos) {
        if (CodeClient.location instanceof Dev dev) {
            dev.getLineStartCache().remove(pos.west());
        }

        if (Config.getConfig().CustomBlockInteractions) {
            ClientLevel world = CodeClient.MC.level;
            CodeClient.MC.getSoundManager().play(new SimpleSoundInstance(SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 2, 0.8F, RandomSource.create(), pos));
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(pos.offset(0, 1, 0), Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(pos.offset(-1, 0, 0), Blocks.AIR.defaultBlockState());
            world.setBlockAndUpdate(pos.offset(0, 0, 1), Blocks.AIR.defaultBlockState());
        }
    }

    public static boolean onClickSlot(Slot slot, int button, ClickType actionType, int syncId, int revision) {
        if (CodeClient.location instanceof Dev) {
            if (CodeClient.onClickSlot(slot,button,actionType,syncId,revision)) return true;
//            if (!slot.hasStack()) return false;
//            ItemStack item = slot.getStack();
//            DFItem dfItem = DFItem.of(item);
//            if (dfItem.hasHypercubeKey("varitem")) {
//                try {
//                    Optional<String> varItem = dfItem.getItemData().getHypercubeStringValue("varitem");
//                    if (varItem.isPresent()) {
//                        if (!Config.getConfig().CustomTagInteraction) return false;
//                        if (actionType == SlotActionType.PICKUP_ALL) return false;
//                        JsonElement varElement = JsonParser.parseString(varItem.get());
//                        if (!varElement.isJsonObject()) return false;
//                        JsonObject varObject = (JsonObject) varElement;
//                        if (!(Objects.equals(varObject.get("id").getAsString(), "bl_tag"))) return false;
//                        if (actionType == SlotActionType.QUICK_MOVE && varObject.get("data").getAsJsonObject().has("variable"))
//                            return false;
//
//                        Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
//                        CodeClient.MC.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(syncId, revision, slot.getIndex(), button, SlotActionType.PICKUP, item, int2ObjectMap));
//
//                        String selected = varObject.get("data").getAsJsonObject().get("option").getAsString();
//                        List<Text> currentLore = dfItem.getLore();
//                        ArrayList<Text> lore = new ArrayList<>(currentLore);
//
//                        int i = 0;
//                        Integer tagStartIndex = null;
//                        Integer selectedIndex = null;
//                        List<String> options = new ArrayList<>();
//
//                        for (Text text : lore) {
//                            if (text == null) return false;
//                            TextColor color = text.getStyle().getColor();
//                            if (color == null) {
//                                List<Text> siblings = text.getSiblings();
//                                if (siblings.size() == 2)
//                                    if (text.getSiblings().get(0).getStyle().getColor().equals(TextColor.fromFormatting(Formatting.DARK_AQUA))) {
//                                        if (tagStartIndex == null) tagStartIndex = i;
//                                        options.add(text.getSiblings().get(1).getString());
//                                        selectedIndex = i;
//                                    }
//                            } else if (color.equals(TextColor.fromRgb(0x808080))) {
//                                if (tagStartIndex == null) tagStartIndex = i;
//                                options.add(text.getString());
//                            }
//                            i++;
//                        }
//
//                        if (selectedIndex == null) return true;
//
//                        int shift = button == 0 ? 1 : -1;
//                        int selectionIndex = selectedIndex - tagStartIndex;
//                        int newSelection = (selectionIndex + shift) % (options.size());
//                        if (newSelection < 0) newSelection = options.size() + newSelection;
//
//                        int optionIndex = 0;
//                        for (String option : options) {
//                            MutableText text = Text.empty();
//                            if (optionIndex == newSelection) {
//                                text.append(Text.literal("» ").setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA).withItalic(false))).append(Text.literal(option).setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
//                            } else
//                                text = Text.literal(option).setStyle(Text.empty().getStyle().withColor(TextColor.fromRgb(0x808080)));
//                            lore.set(tagStartIndex + optionIndex, text);
//                            optionIndex++;
//                        }
//
//                        dfItem.setLore(lore);
//                        slot.setStack(dfItem.getItemStack());
//                        return true;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
        return false;
    }

    public static boolean isInsideWall(Vec3 playerPos) {
        Vec3 middlePos = playerPos.add(0, 0.75, 0);
        AABB box = new EntityDimensions(0.6f, 1.8f, 1.8f * 0.85f, EntityAttachments.createDefault(0.6f, 1.8f), true).makeBoundingBox(playerPos); //Box.of(middlePos, 0.6, 5, 0.6);
        CodeClient.LOGGER.info(String.valueOf(box));
        return BlockPos.betweenClosedStream(box).anyMatch((pos) -> {
            BlockState blockState = CodeClient.MC.level.getBlockState(pos);
            return !blockState.isAir() && Shapes.joinIsNotEmpty(blockState.getCollisionShape(CodeClient.MC.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(box), BooleanOp.AND);
        });
    }

    /**
     * Gets where a df block will place from a hit result.
     *
     * @return The pos for the block behind the sign. Null if the place should've failed.
     */
    @Nullable
    public static BlockPos getPlacePos(BlockHitResult hitResult) {
        BlockState state = CodeClient.MC.level.getBlockState(hitResult.getBlockPos());
        BlockPos pos = state.getBlock() == Blocks.STONE ? hitResult.getBlockPos().south() : hitResult.getBlockPos().relative(hitResult.getDirection());
        if (pos.getY() % 5 != 0) return null;
        Vec3i[] check = {new Vec3i(1, 0, 0), new Vec3i(1, 0, 1), new Vec3i(1, 0, 2), new Vec3i(2, 0, 0), new Vec3i(-1, 0, 0)};
        for (Vec3i offset : check) {
            if (CodeClient.MC.level.getBlockState(pos.offset(offset)).isRedstoneConductor(CodeClient.MC.level, pos.offset(offset)))
                return null;
        }
        return pos;
    }

    public static BlockHitResult onBlockInteract(BlockHitResult hitResult) {
        if (CodeClient.location instanceof Dev plot && plot.isInDev(hitResult.getBlockPos())) {
            CodeClient.onClickChest(hitResult);
            plot.getLineStartCache();
            BlockPos pos = hitResult.getBlockPos();
            if (plot.isInDev(pos)) {
                if (CodeClient.MC.level.getBlockState(pos).getBlock() == Blocks.CHEST) {
                    isOpeningCodeChest = true;
                }
                if (pos.getY() % 5 == 4) { // Is a code space level (glass)
                    if (hitResult.getDirection() == Direction.UP || hitResult.getDirection() == Direction.DOWN) {
                        if (CodeClient.MC.level.getBlockState(pos).isAir() && Config.getConfig().PlaceOnAir)
                            return new BlockHitResult(hitResult.getLocation(), Direction.UP, hitResult.getBlockPos().offset(0, 1, 0), hitResult.isInside());
                        if (Config.getConfig().CustomBlockInteractions)
                            return new BlockHitResult(hitResult.getLocation(), Direction.UP, hitResult.getBlockPos(), hitResult.isInside());
                    }
                }
            }
            if (hitResult.getDirection() == Direction.DOWN) {
                BlockHitResult newHitResult = new BlockHitResult(hitResult.getLocation(), Direction.UP, hitResult.getBlockPos().offset(0, -1, 0), hitResult.isInside());
                return newHitResult;
            }
        }
        return hitResult;
    }

    public static boolean onItemInteract(Player player, InteractionHand hand) {
        if (CodeClient.location instanceof Dev) {
            var feat = CodeClient.getFeature(ScopeSwitcher.ScopeSwitcherFeature.class);
            if (player.isShiftKeyDown() || feat.isEmpty()) return false;

            ItemStack stack = player.getItemInHand(hand);
            DFItem dfItem = DFItem.of(stack);

            if (!dfItem.hasHypercubeKey("varitem")) return false;
            Optional<String> varItem = dfItem.getHypercubeStringValue("varitem");
            if (varItem.isEmpty()) return false;
            JsonObject var = JsonParser.parseString(varItem.get()).getAsJsonObject();
            if (!var.get("id").getAsString().equals("var")) return false;
            JsonObject data = var.get("data").getAsJsonObject();
            String scopeName = data.get("scope").getAsString();

            feat.get().open(scopeName);
            return true;
        }
        return false;
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
//                Vec3d eyeRel = origin.relativize(player.getEntityPos());
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
    public static VoxelShape customVoxelShape(BlockGetter world, BlockPos pos) {
        if (CodeClient.MC != null && CodeClient.MC.player != null && CodeClient.location instanceof Dev plot) {
            Config.LayerInteractionMode mode = Config.getConfig().CodeLayerInteractionMode;
            Boolean isInDev = plot.isInDev(pos);
            var size = plot.assumeSize();
            if (isInDev == null) return null;
            if (!isInDev) {
                var player = CodeClient.MC.player;
                boolean playerIsOutside = player.getEyeY() < 50
                        && ((player.getX() <= plot.getX() - size.codeWidth)
                        || player.getZ() <= (plot.getZ())
                        || (player.getZ() >= plot.getZ() + size.codeLength)
                );
                if (!playerIsOutside) return null;
                boolean blockIsOutside = pos.getY() < 50
                        && (pos.getX() < plot.getX()) && (pos.getX() >= plot.getX() - (size.codeWidth + NoClip.FREEDOM))
                        && (pos.getZ() >= (plot.getZ() - 2)) && (pos.getZ() <= plot.getZ() + plot.assumeSize().codeLength + NoClip.FREEDOM);
                if (blockIsOutside) return Shapes.empty();
                return null;
            }
            boolean isLevel = pos.getY() % 5 == 4;
            boolean noClipAllowsBlock = Config.getConfig().NoClipEnabled || world.getBlockState(pos).isAir();
            boolean hideCodeSpace =
                    pos.getY() >= plot.getFloorY() &&
                            noClipAllowsBlock &&
                            mode != Config.LayerInteractionMode.OFF
                            && isLevel
                            && (
                            mode == Config.LayerInteractionMode.ON
                                    || pos.getY() + 1 < CodeClient.MC.player.getEyeY()
                    )
                            && !world.getBlockState(pos.offset(0, 1, 0)).isRedstoneConductor(world, pos);
            if (hideCodeSpace) return Shapes.box(0, 1 - 1d / (4096), 0, 1, 1, 1);
            if (noClipAllowsBlock && mode != Config.LayerInteractionMode.OFF && pos.getY() + 1 > CodeClient.MC.player.getEyeY() && isLevel)
                return Shapes.empty();
        }
        return null;
    }
}
