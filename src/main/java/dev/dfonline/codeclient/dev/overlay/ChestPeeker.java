package dev.dfonline.codeclient.dev.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.item.Scope;
import dev.dfonline.codeclient.hypercube.item.Variable;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ChestPeeker extends Feature {

    private static final int REQUEST_TIMEOUT = 20;

    // volatile because I don't trust networking.
    private static volatile BlockPos currentBlock = null;
    private static volatile List<ItemStack> items = new ArrayList<>();
    private static volatile boolean itemsFetched = false;
    private int timeOut = 0;
    private static volatile Consumer<List<ItemStack>> currentCallback = null;
    private static volatile boolean expectingItems = false;
    private static volatile int requestTicks = 0;
    private static volatile int requestSlot = -1;

    public static void pick(Consumer<List<ItemStack>> callback) {
        currentCallback = callback;
        currentBlock = null;
        items = new ArrayList<>();
        itemsFetched = false;
    }

    public void tick() {
        if (requestTicks > 0 && --requestTicks == 0) {
            expectingItems = false;
        }
        if (timeOut > 0) {
            timeOut--;
            return;
        }
//        if (CodeClient.MC.currentScreen != null) return;
        if (CodeClient.MC.level == null) return;
        if (!Config.getConfig().ChestPeeker && currentCallback == null) return;
        if (CodeClient.location instanceof Dev dev) {
            if (CodeClient.MC.hitResult instanceof BlockHitResult block) {
                BlockPos pos = block.getBlockPos();
                if (pos.equals(currentBlock)) return;
                if (currentBlock == null && !itemsFetched) { // Use itemsFetched instead of null
                    if (!dev.isInDev(pos)) {
                        return;
                    }
                    if (CodeClient.MC.level.getBlockState(pos).getBlock() != Blocks.CHEST) {
                        return;
                    }
                    // Wait for the previous request to finish, then try again.
                    if (expectingItems) {
                        return;
                    }
                    ClientPacketListener network = CodeClient.MC.getConnection();
                    if (network == null) {
                        return;
                    }

                    currentBlock = pos;
                    items = new ArrayList<>();
                    itemsFetched = false;

                    requestSlot = CodeClient.MC.player.getInventory().getSelectedSlot();
                    network.send(new ServerboundSetCarriedItemPacket(requestSlot));
                    Utility.sendHandItem(ItemStack.EMPTY);
                    network.send(new ServerboundPickItemFromBlockPacket(currentBlock, true));
                    Utility.sendHandItem(CodeClient.MC.player.getMainHandItem());
                    requestTicks = REQUEST_TIMEOUT;
                    expectingItems = true;
                    return;
                }
            }
        }
        currentBlock = null;
        items = new ArrayList<>();
        itemsFetched = false;
    }

    public boolean onReceivePacket(Packet<?> packet) {

        var net = CodeClient.MC.getConnection();
        if (net == null) return false;
        if (!Config.getConfig().ChestPeeker && currentCallback == null) return false;
        if (CodeClient.MC.player == null) return false;
        var inv = CodeClient.MC.player.getInventory();

        if (CodeClient.location instanceof Dev) {
            if (currentBlock != null/* && CodeClient.MC.currentScreen == null*/) {
                if (packet instanceof ClientboundBlockEventPacket block) {
                    if (!Objects.equals(currentBlock, block.getPos())) return false;
                    if (block.getB0() != 1) return false;
                    if (block.getB1() != 0) return false;
                    clear();
                }
            }
            if (expectingItems && packet instanceof ClientboundSetHeldSlotPacket(int slot1)) {
                net.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
                if (slot1 != requestSlot) {
                    ItemContainerContents container = getChestContents(inv.getItem(slot1));
                    if (container != null) receive(container);
                }
                return true;
            }
            if (expectingItems && packet instanceof ClientboundContainerSetSlotPacket slot && slot.getContainerId() == 0) {
                var handler = CodeClient.MC.player.inventoryMenu;

                int slotIndex = slot.getSlot();
                if (slotIndex < 0 || slotIndex >= handler.slots.size()) return false;
                var currentItem = handler.getSlot(slotIndex).getItem();

                ItemContainerContents container = getChestContents(slot.getItem());
                boolean isResponse = slotIndex == 36 + requestSlot && container != null;
                boolean isSwappedOut = slot.getItem().isEmpty() && currentItem.is(Items.CHEST);
                if (!isResponse && !isSwappedOut) {
                    return false;
                }

                net.send(new ServerboundSetCreativeModeSlotPacket(slotIndex, currentItem));
                handler.setItem(slotIndex, 0, currentItem);
                if (isResponse) receive(container);
                return true;
            }
        }
        return false;
    }

    private static @Nullable ItemContainerContents getChestContents(ItemStack stack) {
        if (!stack.is(Items.CHEST)) {
            return null;
        }
        return DFItem.of(stack).getContainer();
    }

    private static void receive(ItemContainerContents container) {
        List<ItemStack> received = new ArrayList<>();
        container.nonEmptyItems().forEach(stack -> received.add(stack.create()));
        items = received;

        itemsFetched = true;
        expectingItems = false;
        requestTicks = 0;

        var callback = currentCallback;
        if (callback != null) {
            currentCallback = null;
            callback.accept(received);
        }
    }

    public List<Component> getOverlayText() {
        if (!Config.getConfig().ChestPeeker) return null;
        if (CodeClient.location instanceof Dev && currentBlock != null) {
            ArrayList<Component> texts = new ArrayList<>();
            if (!itemsFetched) {
                return null;
            } else if (items.isEmpty()) {
                texts.add(Component.translatable("codeclient.peeker.empty").withStyle(ChatFormatting.GOLD));
            } else {
                texts.add(Component.translatable("codeclient.peeker.contents").withStyle(ChatFormatting.GOLD));
                for (ItemStack item : items) {
                    DFItem dfItem = DFItem.of(item);
                    List<Component> currentLore = dfItem.getLore();
                    ArrayList<Component> lore = new ArrayList<>(currentLore);


                    MutableComponent text = Component.empty();
                    text.append(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY));
                    Optional<String> varItem = dfItem.getHypercubeStringValue("varitem");
                    if (varItem.isEmpty()) {
                        text.append(item.getCount() + "x ");
                        text.append(item.getHoverName());
                    } else {
                        JsonObject object = JsonParser.parseString(varItem.get()).getAsJsonObject();
                        try {
                            Type type = Type.valueOf(object.get("id").getAsString());
                            JsonObject data = object.get("data").getAsJsonObject();
                            text.append(Component.literal(type.name.toUpperCase()).withStyle(Style.EMPTY.withColor(type.color)).append(" "));
                            if (type == Type.var) {
                                Scope scope = Scope.valueOf(data.get("scope").getAsString());
                                text.append(scope.getShortName()).withStyle(Style.EMPTY.withColor(scope.color)).append(" ");
                            }
                            if (type == Type.num || type == Type.txt || type == Type.comp || type == Type.var || type == Type.g_val || type == Type.pn_el) {
                                text.append(item.getHoverName());
                            }
                            if (type == Type.loc) {
                                JsonObject loc = data.get("loc").getAsJsonObject();
                                text.append("[%.2f, %.2f, %.2f, %.2f, %.2f]".formatted(
                                        loc.get("x").getAsFloat(),
                                        loc.get("y").getAsFloat(),
                                        loc.get("z").getAsFloat(),
                                        loc.get("pitch").getAsFloat(),
                                        loc.get("yaw").getAsFloat()));
                            }
                            if (type == Type.vec) {
                                text.append(Component.literal("<%.2f, %.2f, %.2f>".formatted(
                                        data.get("x").getAsFloat(),
                                        data.get("y").getAsFloat(),
                                        data.get("z").getAsFloat())
                                ).withStyle(Style.EMPTY.withColor(Type.vec.color)));
                            }
                            if (type == Type.snd) {
                                text.append(lore.getFirst());
                                text.append(Component.literal(" P: ").withStyle(ChatFormatting.GRAY));
                                text.append(Component.literal("%.1f".formatted(data.get("pitch").getAsFloat())));
                                text.append(Component.literal(" V: ").withStyle(ChatFormatting.GRAY));
                                text.append(Component.literal("%.1f".formatted(data.get("vol").getAsFloat())));
                            }
                            if (type == Type.part) {
                                text.append(Component.literal("%dx ".formatted(data.get("cluster").getAsJsonObject().get("amount").getAsInt())));
                                text.append(lore.getFirst());
                            }
                            if (type == Type.pot) {
                                text.append(lore.getFirst());
                                text.append(Component.literal(" %d ".formatted(data.get("amp").getAsInt() + 1)));
                                int dur = data.get("dur").getAsInt();
                                text.append(dur >= 1000000 ? "Infinite" : dur % 20 == 0 ? "%d:%02d".formatted((dur / 1200), (dur / 20) % 60) : (dur + "ticks"));
                            }
                            if (type == Type.bl_tag) {
                                text.append(Component.literal(data.get("tag").getAsString()).withStyle(ChatFormatting.YELLOW));
                                text.append(Component.literal(" » ").withStyle(ChatFormatting.DARK_AQUA));
                                if (data.has("variable")) {
                                    Variable variable = new Variable(data.getAsJsonObject("variable"));
                                    Scope scope = variable.getScope();
                                    text.append(Component.literal(scope.getShortName() + " ").withStyle(Style.EMPTY.withColor(scope.color)));
                                    text.append(Component.literal(variable.getName()));
                                } else {
                                    text.append(Component.literal(data.get("option").getAsString()).withStyle(ChatFormatting.AQUA));
                                }
                            }
                            if (type == Type.hint) continue;
                        } catch (IllegalArgumentException ignored) {
                            text.append(Component.literal(object.get("id").getAsString().toUpperCase())
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0x808080)))
                                    .append(" "));
                            text.append(item.getHoverName());
                        }
                    }
                    texts.add(text);
                }
            }
            return texts;
        }
        return null;
    }

    @Override
    public void onBreakBlock(@NotNull Dev dev, @NotNull BlockPos pos, @Nullable BlockPos breakPos) {
        clear();
    }

    @Override
    public void onClickChest(BlockHitResult hitResult) {
//        clear();
    }

    private void clear() {
        items = new ArrayList<>();
        itemsFetched = false;
        currentBlock = null;
        timeOut = 10;
        currentCallback = null;
    }

    public void reset() {
        clear();
        expectingItems = false;
        requestTicks = 0;
    }

    enum Type {
        txt("str", ChatFormatting.AQUA),
        comp("txt", TextColor.fromRgb(0x7fd42a)),
        num("num", ChatFormatting.RED),
        loc("loc", ChatFormatting.GREEN),
        vec("vec", TextColor.fromRgb(0x2affaa)),
        snd("snd", ChatFormatting.BLUE),
        part("par", TextColor.fromRgb(0xaa55ff)),
        pot("pot", TextColor.fromRgb(0xff557f)),
        var("var", ChatFormatting.YELLOW),
        g_val("val", TextColor.fromRgb(0xffd47f)),
        pn_el("param", TextColor.fromRgb(0xaaffaa)),
        bl_tag("tag", ChatFormatting.YELLOW),
        hint("hint", TextColor.fromRgb(0xaaff55));

        public final String name;
        public final TextColor color;

        Type(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }

        Type(String name, ChatFormatting color) {
            this.name = name;
            this.color = TextColor.fromLegacyFormat(color);
        }
    }

}
