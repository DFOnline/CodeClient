package dev.dfonline.codeclient.dev.menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RecentValues extends Feature {
    private final Path file = FileManager.recentValuesPath();
    private final List<ItemStack> pinned = new ArrayList<>();
    private final List<ItemStack> recent = new ArrayList<>();
    private ItemStack hoveredItem = null;
    private List<ItemStack> hoveredOrigin = null;

    @Override
    public boolean enabled() {
        return Config.getConfig().RecentValues > 0;
    }

    public RecentValues() {
        try {
            if (Files.exists(file)) {
                JsonObject data = JsonParser.parseString(Files.readString(file)).getAsJsonObject();

                int version = data.get("version").getAsInt();
                JsonArray pinnedJson = data.getAsJsonArray("pinned");
                JsonArray recentJson = data.getAsJsonArray("recent");

                for (JsonElement item : pinnedJson) {
                    pinned.add(readItem(version, item));
                }
                for (JsonElement item : recentJson) {
                    recent.add(readItem(version, item));
                }
            }

            ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
                try {
                    if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
                    JsonObject data = new JsonObject();
                    data.addProperty("version", SharedConstants.getCurrentVersion().dataVersion().version());
                    data.add("pinned", saveItems(pinned));
                    data.add("recent", saveItems(recent));
                    Files.writeString(file, data.toString());
                } catch (Exception err) {
                    CodeClient.LOGGER.error("Failed to save recent_values.json!");
                    err.printStackTrace();
                }
            });
        } catch (Exception err) {
            CodeClient.LOGGER.error("Failed reading recent_values.json!", err);
        }
    }

    private JsonArray saveItems(List<ItemStack> list) {
        JsonArray out = new JsonArray();

        if (CodeClient.MC.level == null) throw new RuntimeException("World is null!");

        for (ItemStack item : list) {
            out.add(ItemStack.CODEC.encodeStart(CodeClient.MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), item).getOrThrow().toString());
        }

        return out;
    }

    private ItemStack readItem(int version, JsonElement item) throws Exception {
        if (CodeClient.MC.level == null) return null;
        var fromNbt = ItemStack.CODEC.decode(CodeClient.MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), DataFixTypes.HOTBAR.updateToCurrentVersion(CodeClient.MC.getFixerUpper(), TagParser.parseCompoundFully(item.getAsString()), version));
        return fromNbt.result().map(Pair::getFirst).orElse(null);
    }

    public void remember(ItemStack item) {
        if (CodeClient.MC.level == null) return;

        DFItem dfItem = DFItem.of(item);
        if (!(CodeClient.location instanceof Dev) || dfItem.getHypercubeStringValue("varitem").isEmpty()) return;
        for (ItemStack it : pinned) {
            if (it != null && item.getItem() == it.getItem() && item.equals(it)) return;
        }

        ItemStack lambdaItem = item;
        if (item.getItem() == null) {
            recent.remove(item);
            return;
        }
        recent.removeIf(it -> it != null && lambdaItem.getItem() == it.getItem() && ItemStack.CODEC.encodeStart(CodeClient.MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), lambdaItem).getOrThrow().equals(ItemStack.CODEC.encodeStart(CodeClient.MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), it).getOrThrow()));
        item = item.copyWithCount(1);
        recent.add(0, item);

        while (recent.size() > Config.getConfig().RecentValues) {
            recent.remove(recent.size() - 1);
        }
    }

    @Override
    public ChestFeature makeChestFeature(AbstractContainerScreen<?> screen) {
        return new RecentValuesOverlay(screen);
    }

    class RecentValuesOverlay extends ChestFeature {
        public RecentValuesOverlay(AbstractContainerScreen<?> screen) {
            super(screen);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, int screenX, int screenY, float delta) {
            hoveredItem = null;
            if(recent.isEmpty() && pinned.isEmpty()) return;
            int xEnd = 16 * 20;

            context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("recipe_book/overlay_recipe"), -screenX + 6, -5,
                    Math.min(Math.max(pinned.size(), recent.size()), 16) * 20 + 10,
                    (((int) Math.ceil((double) pinned.size() / 16)) + ((int) Math.ceil((double) recent.size() / 16))) * 16 + 10
            );

            hoveredItem = null;
            hoveredOrigin = null;
            int y = screenY;
            for (List<ItemStack> group : List.of(pinned,recent)) {
                int x = 13;
                for (ItemStack item : group) {
                    if (item == null) continue;
                    context.renderItem(item, x - screenX, y - screenY);
                    context.renderItemDecorations(CodeClient.MC.font, item, x - screenX, y - screenY);
                    if (mouseX > x && mouseY > y && mouseX < x + 15 && mouseY < y + 15) {
                        context.setTooltipForNextFrame(CodeClient.MC.font, item, mouseX, mouseY);
                        hoveredItem = item;
                        hoveredOrigin = group;
                    }
                    x += 20;
                    if (x > xEnd) {
                        x = 13;
                        y += 15;
                    }
                }
                if (x != 13) y += 15;
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click) {
            if (hoveredItem == null) return false;

            if (click.button() != 1) {
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.hasItem()) continue;
                    CodeClient.MC.getSoundManager().play(new SimpleSoundInstance(
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            2, 1f, RandomSource.create(),
                            CodeClient.MC.player.blockPosition()
                    ));

                    if (!CodeClient.MC.player.isCreative()) return false;
                    ItemStack previous = CodeClient.MC.player.getInventory().getItem(0);
                    CodeClient.MC.gameMode.handleCreativeModeItemAdd(hoveredItem, 36);
                    CodeClient.MC.gameMode.handleContainerInput(
                            screen.getMenu().containerId,
                            slot.index, 0, ContainerInput.SWAP, CodeClient.MC.player
                    );
                    CodeClient.MC.gameMode.handleCreativeModeItemAdd(previous, 36);
                    return true;
                }
            } else {
                hoveredOrigin.remove(hoveredItem);
                if (hoveredOrigin == pinned) {
                    CodeClient.MC.getSoundManager().play(new SimpleSoundInstance(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SoundSource.PLAYERS,
                            2, 0.5f, RandomSource.create(),
                            CodeClient.MC.player.blockPosition()
                    ));
                    return true;
                }
                CodeClient.MC.getSoundManager().play(new SimpleSoundInstance(
                        SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.PLAYERS,
                        2, 0.6f, RandomSource.create(),
                        CodeClient.MC.player.blockPosition()
                ));
                pinned.add(hoveredItem);
                return true;
            }
            return false;
        }
    }
}
