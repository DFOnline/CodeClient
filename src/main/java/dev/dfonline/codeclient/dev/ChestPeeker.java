package dev.dfonline.codeclient.dev;

import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.OverlayManager;
import dev.dfonline.codeclient.location.Dev;
import dev.dfonline.codeclient.mixin.world.ClientWorldAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

import java.util.ArrayList;
import java.util.Objects;

public class ChestPeeker {
    private static BlockPos currentBlock = null;
    private static NbtList items = null;

    public static void tick() {
        if(CodeClient.location instanceof Dev dev) {
            if(CodeClient.MC.crosshairTarget instanceof BlockHitResult block) {
                BlockPos pos = block.getBlockPos();
                if(pos.equals(currentBlock)) return;
                if(currentBlock == null && items == null) {
                    if (!dev.isInDev(pos)) {
                        return;
                    }
                    if (CodeClient.MC.world.getBlockState(pos).getBlock() != Blocks.CHEST) {
                        return;
                    }
                    currentBlock = pos;
                    items = null;

                    ItemStack item = Items.CHEST.getDefaultStack();
                    NbtCompound bet = new NbtCompound();
                    bet.put("Items", new NbtList());
                    bet.putString("id", "minecraft:chest");
                    bet.putInt("x", pos.getX());
                    bet.putInt("y", pos.getY());
                    bet.putInt("z", pos.getZ());
                    item.setSubNbt("BlockEntityTag", bet);
                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1,ItemStack.EMPTY));
                    CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1, item));
                    return;
                }
            }
        }
        currentBlock = null;
        items = null;
    }

    /**
     * @return true to cancel packet.
     */
    public static <T extends PacketListener> boolean handlePacket(Packet<T> packet) {
        if(CodeClient.location instanceof Dev dev) {
            if(currentBlock == null) return false;
            if(packet instanceof ScreenHandlerSlotUpdateS2CPacket slot) {
                CodeClient.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1,ItemStack.EMPTY));
                if(slot.getSlot() != 1) return false;
                NbtCompound nbt = slot.getItemStack().getNbt();
                if(nbt == null) return false;
                NbtCompound bet = nbt.getCompound("BlockEntityTag");
                if(bet == null) return false;
                if(!Objects.equals(bet.getString("id"), "minecraft:chest")) return false;
                items = bet.getList("Items", NbtElement.COMPOUND_TYPE);
                return true;
            }
        }
        return false;
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
        if (CodeClient.location instanceof Dev && currentBlock != null && items != null) {
            ArrayList<Text> texts = new ArrayList<>();
            if(items.isEmpty()) {
                texts.add(Text.literal("Empty Chest").formatted(Formatting.GOLD));
            }
            else {
                texts.add(Text.literal("Chest").formatted(Formatting.GOLD));
                for (NbtElement itemData : items) {
                    if (itemData instanceof NbtCompound compound) {
                        ItemStack item = Registries.ITEM.get(Identifier.tryParse(compound.getString("id"))).getDefaultStack();
                        item.setCount(compound.getInt("count"));
                        item.setNbt(compound.getCompound("tag"));
                        MutableText text = Text.empty();
                        text.append(Text.literal(" • ").formatted(Formatting.DARK_GRAY));
                        text.append(item.getName());
                        texts.add(text);
                    }
                }
            }

            TextRenderer textRenderer = CodeClient.MC.textRenderer;
            int widest = 0;
            for (Text text: texts) {
                int width = textRenderer.getWidth(text);
                if(width > widest) widest = width;
            }
            widest = Math.max(widest,45);

            Camera camera = CodeClient.MC.gameRenderer.getCamera();
            if (camera.isReady() && CodeClient.MC.getEntityRenderDispatcher().gameOptions != null) {
                double camX = camera.getPos().x;
                double camY = camera.getPos().y;
                double camZ = camera.getPos().z;
                matrices.push();
                matrices.translate((float)(currentBlock.getX() - camX) + 0.5, (float)(currentBlock.getY() - camY) + 0.4F, (float)(currentBlock.getZ() - camZ) + 0.5);
                matrices.multiplyPositionMatrix((new Matrix4f()).rotation(camera.getRotation()));
                float size = 0.02f;
                matrices.scale(-size, -size, size);
                float x = (float)(-widest) / 2.0F;
//                width -= 0 / size;
                float y = texts.size() * -4.5F;
                    Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                    matrix4f.translate(x,y,0F);
                    VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getTextBackgroundSeeThrough());
                    int color = 0xFF_00_00_00;
                    vertexConsumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(color).light(15728880).next();
                    vertexConsumer.vertex(matrix4f, -1.0F, (float)texts.size() * 9, 0.0F).color(color).light(15728880).next();
                    vertexConsumer.vertex(matrix4f, (float)widest, (float)texts.size() * 9, 0.0F).color(color).light(15728880).next();
                    vertexConsumer.vertex(matrix4f, (float)widest, -1.0F, 0.0F).color(color).light(15728880).next();
                    matrix4f.translate(-x,-y,0);
                for (Text text: texts) {
                    textRenderer.draw(text, x, y, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
                    y += textRenderer.fontHeight;
                }
                matrices.pop();
            }
        }
    }
}
