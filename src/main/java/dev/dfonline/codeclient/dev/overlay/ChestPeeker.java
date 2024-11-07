package dev.dfonline.codeclient.dev.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.hypercube.item.Scope;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChestPeeker extends Feature {
    private BlockPos currentBlock = null;
    private ArrayList<ItemStack> items = null;
    private boolean shouldClearChest = false;
    private int timeOut = 0;

    public void tick() {
        if (timeOut > 0) {
            timeOut--;
            return;
        }
        if (CodeClient.MC.currentScreen != null) return;
        if (CodeClient.MC.world == null) return;
        if (!Config.getConfig().ChestPeeker) return;
        if (CodeClient.location instanceof Dev dev) {
            if (CodeClient.MC.crosshairTarget instanceof BlockHitResult block) {
                BlockPos pos = block.getBlockPos();
                if (pos.equals(currentBlock)) return;
                if (currentBlock == null && items == null) {
                    if (!dev.isInDev(pos)) {
                        return;
                    }
                    if (CodeClient.MC.world.getBlockState(pos).getBlock() != Blocks.CHEST) {
                        return;
                    }
                    currentBlock = pos;
                    items = null;
                    shouldClearChest = true;

                    // Uses non-abstracted item changes due to this very specific use case.
                    ItemStack item = Items.CHEST.getDefaultStack();
                    NbtCompound bet = new NbtCompound();
                    bet.putString("id", "minecraft:chest");
                    bet.putInt("x", pos.getX());
                    bet.putInt("y", pos.getY());
                    bet.putInt("z", pos.getZ());
                    item.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(bet));
                    item.set(DataComponentTypes.CUSTOM_NAME, Text.literal("CodeClient chest peeker internal"));

                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1, ItemStack.EMPTY));
                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1, item));
                    return;
                }
            }
        }
        currentBlock = null;
        items = null;
    }

    public boolean onReceivePacket(Packet<?> packet) {
        if (CodeClient.MC.currentScreen != null) return false;
        if (CodeClient.MC.getNetworkHandler() == null) return false;
        if (!Config.getConfig().ChestPeeker) return false;
        if (CodeClient.location instanceof Dev) {
            if (packet instanceof BlockEventS2CPacket block) {
                if (!Objects.equals(currentBlock, block.getPos())) return false;
                if (block.getType() != 1) return false;
                if (block.getData() != 0) return false;
                reset();
            }
            if (packet instanceof ScreenHandlerSlotUpdateS2CPacket slot) {
                DFItem item = DFItem.of(slot.getStack());
                if (!Objects.equals(item.getName(), Text.literal("CodeClient chest peeker internal"))) return false;
                ContainerComponent container = item.getContainer();
                if (container == null) return false;
                items = new ArrayList<>();
                container.iterateNonEmpty().forEach((stack) -> {
                    items.add(stack);
                });
                CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
                shouldClearChest = false;
                return true;
            }
        }
        return false;
    }

    public List<Text> getOverlayText() {
        if (!Config.getConfig().ChestPeeker) return null;
        if (CodeClient.location instanceof Dev && currentBlock != null && items != null) {
            ArrayList<Text> texts = new ArrayList<>();
            if (items.isEmpty()) {
                texts.add(Text.translatable("codeclient.peeker.empty").formatted(Formatting.GOLD));
            } else {
                texts.add(Text.translatable("codeclient.peeker.contents").formatted(Formatting.GOLD));
                for (ItemStack item : items) {
                    DFItem dfItem = DFItem.of(item);
                    List<Text> currentLore = dfItem.getLore();
                    ArrayList<Text> lore = new ArrayList<>(currentLore);


                    MutableText text = Text.empty();
                        text.append(Text.literal(" • ").formatted(Formatting.DARK_GRAY));
                    String varItem = dfItem.getHypercubeStringValue("varitem");
                        if (Objects.equals(varItem, "")) {
                            text.append(item.getCount() + "x ");
                            text.append(item.getName());
                        } else {
                            try {
                                JsonObject object = JsonParser.parseString(varItem).getAsJsonObject();
                                Type type = Type.valueOf(object.get("id").getAsString());
                                JsonObject data = object.get("data").getAsJsonObject();
                                //                            JsonArray lore = data.get("display").getAsJsonObject().get("Lore").getAsJsonArray();
                                text.append(Text.literal(type.name.toUpperCase()).fillStyle(Style.EMPTY.withColor(type.color)).append(" "));
                                if (type == Type.var) {
                                    Scope scope = Scope.valueOf(data.get("scope").getAsString());
                                    text.append(scope.getShortName()).fillStyle(Style.EMPTY.withColor(scope.color)).append(" ");
                                }
                                if (type == Type.num || type == Type.txt || type == Type.comp || type == Type.var || type == Type.g_val || type == Type.pn_el) {
                                    text.append(item.getName());
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
                                    text.append(Text.literal("<%.2f, %.2f, %.2f>".formatted(
                                            data.get("x").getAsFloat(),
                                            data.get("y").getAsFloat(),
                                            data.get("z").getAsFloat())
                                    ).fillStyle(Style.EMPTY.withColor(Type.vec.color)));
                                }
                                if (type == Type.snd) {
                                    text.append(lore.getFirst());
                                    text.append(Text.literal(" P: ").formatted(Formatting.GRAY));
                                    text.append(Text.literal("%.1f".formatted(data.get("pitch").getAsFloat())));
                                    text.append(Text.literal(" V: ").formatted(Formatting.GRAY));
                                    text.append(Text.literal("%.1f".formatted(data.get("vol").getAsFloat())));
                                }
                                if (type == Type.part) {
                                    text.append(Text.literal("%dx ".formatted(data.get("cluster").getAsJsonObject().get("amount").getAsInt())));
                                    text.append(lore.getFirst());
                                }
                                if (type == Type.pot) {
                                    text.append(lore.getFirst());
                                    text.append(Text.literal(" %d ".formatted(data.get("amp").getAsInt() + 1)));
                                    int dur = data.get("dur").getAsInt();
                                    text.append(dur >= 1000000 ? "Infinite" : dur % 20 == 0 ? "%d:%02d".formatted((dur / 1200), (dur / 20) % 60) : (dur + "ticks"));
                                }
                                if (type == Type.bl_tag) {
                                    text.append(Text.literal(data.get("tag").getAsString()).formatted(Formatting.YELLOW));
                                    text.append(Text.literal(" » ").formatted(Formatting.DARK_AQUA));
                                    text.append(Text.literal(data.get("option").getAsString()).formatted(Formatting.AQUA));
                                }
                                if (type == Type.hint) continue;
                            } catch (Exception ignored) {
                                continue;
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
        reset();
    }

    @Override
    public void onClickChest(BlockHitResult hitResult) {
        reset();
    }

    public void reset() {
        items = null;
        shouldClearChest = true;
        currentBlock = null;
        timeOut = 10;
    }

    enum Type {
        txt("str", Formatting.AQUA),
        comp("txt", TextColor.fromRgb(0x7fd42a)),
        num("num", Formatting.RED),
        loc("loc", Formatting.GREEN),
        vec("vec", TextColor.fromRgb(0x2affaa)),
        snd("snd", Formatting.BLUE),
        part("par", TextColor.fromRgb(0xaa55ff)),
        pot("pot", TextColor.fromRgb(0xff557f)),
        var("var", Formatting.YELLOW),
        g_val("val", TextColor.fromRgb(0xffd47f)),
        pn_el("param", TextColor.fromRgb(0xaaffaa)),
        bl_tag("tag", Formatting.YELLOW),
        hint("hint", TextColor.fromRgb(0xaaff55));

        public final String name;
        public final TextColor color;

        Type(String name, TextColor color) {
            this.name = name;
            this.color = color;
        }

        Type(String name, Formatting color) {
            this.name = name;
            this.color = TextColor.fromFormatting(color);
        }
    }
}
