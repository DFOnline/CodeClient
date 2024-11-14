package dev.dfonline.codeclient.dev.menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.ChestFeature;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.Feature;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.data.DFItem;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

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
                    data.addProperty("version", SharedConstants.getGameVersion().getSaveVersion().getId());
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

        if (CodeClient.MC.world == null) throw new RuntimeException("World is null!");

        for (ItemStack item : list) {
            out.add(item.toNbt(CodeClient.MC.world.getRegistryManager()).toString());
        }

        return out;
    }

    private ItemStack readItem(int version, JsonElement item) throws Exception {
        if (CodeClient.MC.world == null) return null;
        var fromNbt = ItemStack.fromNbt(CodeClient.MC.world.getRegistryManager(), DataFixTypes.HOTBAR.update(CodeClient.MC.getDataFixer(), StringNbtReader.parse(item.getAsString()), version));
        return fromNbt.orElse(null);
    }

    public void remember(ItemStack item) {
        DFItem dfItem = DFItem.of(item);
        if (!(CodeClient.location instanceof Dev) || dfItem.getHypercubeStringValue("varitem").isEmpty()) return;
        for (ItemStack it : pinned) {
            if (item.getItem() == it.getItem() && item.equals(it)) return;
        }

        ItemStack lambdaItem = item;
        recent.removeIf(it -> lambdaItem.getItem() == it.getItem() && lambdaItem.equals(it));
        item = item.copyWithCount(1);
        recent.add(0, item);

        while (recent.size() > Config.getConfig().RecentValues) {
            recent.remove(recent.size() - 1);
        }
    }

    @Override
    public ChestFeature makeChestFeature(HandledScreen<?> screen) {
        return new RecentValuesOverlay(screen);
    }

    class RecentValuesOverlay extends ChestFeature {
        public RecentValuesOverlay(HandledScreen<?> screen) {
            super(screen);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, int screenX, int screenY, float delta) {
            hoveredItem = null;
            if(recent.isEmpty() && pinned.isEmpty()) return;
            int xEnd = 16*15;

            context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("recipe_book/overlay_recipe"), -screenX + 6, -5,
                    Math.min(Math.max(pinned.size(), recent.size()),16) * 15 + 10,
                    (((int) Math.ceil((double) pinned.size() / 16)) + ((int) Math.ceil((double) recent.size() / 16))) * 16 + 10
            );

            hoveredItem = null;
            hoveredOrigin = null;
            int y = screenY;
            for (List<ItemStack> group : List.of(pinned,recent)) {
                int x = 10;
                for (ItemStack item : group) {
                    context.drawItem(item, x - screenX, y - screenY);
                    context.drawStackOverlay(CodeClient.MC.textRenderer, item, x - screenX, y - screenY);
                    if (mouseX > x && mouseY > y && mouseX < x + 15 && mouseY < y + 15) {
                        context.drawItemTooltip(CodeClient.MC.textRenderer, item, mouseX - screenX, mouseY - screenY);
                        hoveredItem = item;
                        hoveredOrigin = group;
                    }
                    x += 15;
                    if (x > xEnd) {
                        x = 10;
                        y += 15;
                    }
                }
                if (x != 10) y += 15;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (hoveredItem == null) return false;

            if (button != 1) {
                for (Slot slot : screen.getScreenHandler().slots) {
                    if (slot.hasStack()) continue;
                    CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(
                            SoundEvents.ENTITY_ITEM_PICKUP,
                            SoundCategory.PLAYERS,
                            2, 1f, Random.create(),
                            CodeClient.MC.player.getBlockPos()
                    ));

                    if (!CodeClient.MC.player.isCreative()) return false;
                    ItemStack previous = CodeClient.MC.player.getInventory().getStack(0);
                    CodeClient.MC.interactionManager.clickCreativeStack(hoveredItem, 36);
                    CodeClient.MC.interactionManager.clickSlot(
                            screen.getScreenHandler().syncId,
                            slot.id, 0, SlotActionType.SWAP, CodeClient.MC.player
                    );
                    CodeClient.MC.interactionManager.clickCreativeStack(previous, 36);
                    return true;
                }
            } else {
                hoveredOrigin.remove(hoveredItem);
                if (hoveredOrigin == pinned) {
                    CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SoundCategory.PLAYERS,
                            2, 0.5f, Random.create(),
                            CodeClient.MC.player.getBlockPos()
                    ));
                    return true;
                }
                CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(
                        SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundCategory.PLAYERS,
                        2, 0.6f, Random.create(),
                        CodeClient.MC.player.getBlockPos()
                ));
                pinned.add(hoveredItem);
                return true;
            }
            return false;
        }
    }
}
