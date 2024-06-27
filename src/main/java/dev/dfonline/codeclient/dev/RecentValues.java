package dev.dfonline.codeclient.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.FileManager;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.location.Dev;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

public class RecentValues {

    private static final Path file = FileManager.Path().resolve("recent_values.json");
    private static final List<ItemStack> pinned = new ArrayList<>();
    private static final List<ItemStack> recent = new ArrayList<>();
    private static ItemStack hoveredItem = null;
    private static List<ItemStack> hoveredOrigin = null;

    static {
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
            CodeClient.LOGGER.error("Failed reading recent_values.json!");
            err.printStackTrace();
        }

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GenericContainerScreen container) {
                if (!InsertOverlay.isCodeChest) return;
                if (!(CodeClient.location instanceof Dev)) return;

                ScreenEvents.afterRender(screen).register((screen1, ctx, mouseX, mouseY, tickDelta) -> {
                    if(recent.isEmpty() && pinned.isEmpty()) return;

                    int y = (int) (scaledHeight * 0.25);
                    int xEnd = (int) (scaledWidth * 0.25);

                    ctx.drawGuiTexture(new Identifier("recipe_book/overlay_recipe"), 5, y - 5,
                            Math.min(Math.max(pinned.size(), recent.size()),16) * 15 + 10,
                            (((int) Math.ceil((double) pinned.size() / 16)) + ((int) Math.ceil((double) recent.size() / 16))) * 16 + 10
                            );

                    hoveredItem = null;
                    hoveredOrigin = null;
                    for (List<ItemStack> group : List.of(pinned, recent)) {
                        int x = 10;
                        for (ItemStack item : group) {
                            ctx.drawItem(item, x, y);
                            ctx.drawItemInSlot(CodeClient.MC.textRenderer, item, x, y);
                            if (mouseX > x && mouseY > y && mouseX < x + 15 && mouseY < y + 15) {
                                ctx.drawItemTooltip(CodeClient.MC.textRenderer, item, mouseX, mouseY);
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
                });

                ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (hoveredItem == null) return;

                    if (button != 1) {
                        for (Slot slot : container.getScreenHandler().slots) {
                            if (slot.hasStack()) continue;
                            CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(
                                    SoundEvents.ENTITY_ITEM_PICKUP,
                                    SoundCategory.PLAYERS,
                                    2, 1f, Random.create(),
                                    CodeClient.MC.player.getBlockPos()
                            ));

                            if (!CodeClient.MC.player.isCreative()) return;
                            ItemStack previous = CodeClient.MC.player.getInventory().getStack(0);
                            CodeClient.MC.interactionManager.clickCreativeStack(hoveredItem, 36);
                            CodeClient.MC.interactionManager.clickSlot(
                                    container.getScreenHandler().syncId,
                                    slot.id, 0, SlotActionType.SWAP, CodeClient.MC.player
                            );
                            CodeClient.MC.interactionManager.clickCreativeStack(previous, 36);
                            return;
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
                            return;
                        }
                        CodeClient.MC.getSoundManager().play(new PositionedSoundInstance(
                                SoundEvents.UI_BUTTON_CLICK.value(),
                                SoundCategory.PLAYERS,
                                2, 0.6f, Random.create(),
                                CodeClient.MC.player.getBlockPos()
                        ));
                        pinned.add(hoveredItem);
                    }
                });
            }
        });
    }

    private static JsonArray saveItems(List<ItemStack> list) {
        JsonArray out = new JsonArray();

        for (ItemStack item : list) {
            out.add(item.writeNbt(new NbtCompound()).asString());
        }

        return out;
    }

    private static ItemStack readItem(int version, JsonElement item) throws Exception {
        return ItemStack.fromNbt(DataFixTypes.HOTBAR.update(CodeClient.MC.getDataFixer(), StringNbtReader.parse(item.getAsString()), version));
    }

    public static void remember(ItemStack item) {
        if (item.getNbt() == null || item.getNbt().get("PublicBukkitValues") == null || item.getSubNbt("PublicBukkitValues").get("hypercube:varitem") == null) return;
        for (ItemStack it : pinned) {
            if (item.getItem() == it.getItem() && item.getNbt().equals(it.getNbt())) return;
        }

        ItemStack lambdaItem = item;
        recent.removeIf(it -> lambdaItem.getItem() == it.getItem() && lambdaItem.getNbt().equals(it.getNbt()));
        item = item.copyWithCount(1);
        recent.add(0, item);

        while (recent.size() > Config.getConfig().RecentValues) {
            recent.remove(recent.size() - 1);
        }
    }
}
