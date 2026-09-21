package dev.dfonline.codeclient.data;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents an item, with configurable data, lore, name, and more.
 */
public class DFItem {
    ItemStack item;
    ItemData data;

    private static final SequencedSet<DataComponentType<?>> HIDDEN_COMPONENTS_PREVIOUSLY = ReferenceLinkedOpenHashSet.of(
            DataComponents.BANNER_PATTERNS,
            DataComponents.BEES,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.BLOCK_STATE,
            DataComponents.BUNDLE_CONTENTS,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.CONTAINER,
            DataComponents.CONTAINER_LOOT,
            DataComponents.FIREWORK_EXPLOSION,
            DataComponents.FIREWORKS,
            DataComponents.INSTRUMENT,
            DataComponents.JUKEBOX_PLAYABLE,
            DataComponents.MAP_ID,
            DataComponents.PAINTING_VARIANT,
            DataComponents.POT_DECORATIONS,
            DataComponents.POTION_CONTENTS,
            DataComponents.TROPICAL_FISH_PATTERN,
            DataComponents.WRITTEN_BOOK_CONTENT
    );

    /**
     * Creates a new DFItem from an ItemStack.
     *
     * @param item The item to create the DFItem from.
     */
    public DFItem(ItemStack item) {
        this.item = item;
        this.data = new ItemData(item);
    }

    /**
     * Creates a new DFItem from an ItemStack.
     *
     * @param item The item to create the DFItem from.
     * @return The new DFItem.
     */
    public static DFItem of(ItemStack item) {
        return new DFItem(item);
    }

    /**
     * Gets the item's data.
     *
     * @return The item's data.
     */
    public ItemData getItemData() {
        return data;
    }

    /**
     * Sets the item's data.
     *
     * @param itemData The new data to set.
     */
    public void setItemData(ItemData itemData) {
        data = itemData;
    }

    /**
     * Edits the item's data with a consumer, creates the data if it doesn't exist.
     * <br>
     * Example:
     * <pre>{@code
     * item.editData(data -> {
     *    data.setStringValue("key", "value");
     * });
     * }</pre>
     *
     * @param consumer The consumer to edit the data with.
     */
    public void editData(Consumer<ItemData> consumer) {
        if (!data.hasCustomData()) data = ItemData.getEmpty();
        consumer.accept(data);
    }

    /**
     * Delegates to {@link ItemData#getHypercubeStringValue(String)}.
     *
     * @param key The key to get, without the hypercube: prefix.
     * @return The value of the key, or an empty string if it doesn't exist.
     */
    public Optional<String> getHypercubeStringValue(String key) {
        var itemData = getItemData();
        if (itemData == null) return Optional.empty();
        return itemData.getHypercubeStringValue(key);
    }

    /**
     * Delegates to {@link ItemData#hasHypercubeKey(String)}.
     *
     * @param key The key to check, without the hypercube: prefix.
     * @return Whether the key exists.
     */
    public boolean hasHypercubeKey(String key) {
        var itemData = getItemData();
        if (itemData == null) return false;
        return itemData.hasHypercubeKey(key);
    }

    /**
     * Converts the DFItem back into an ItemStack.
     *
     * @return The ItemStack.
     */
    public ItemStack getItemStack() {
        if (data != null) item.set(DataComponents.CUSTOM_DATA, getItemData().toComponent());
        return item;
    }

    /**
     * Delegates to {@link ItemData#getPublicBukkitValues()}.
     *
     * @return The PublicBukkitValues.
     */
    public PublicBukkitValues getPublicBukkitValues() {
        return getItemData().getPublicBukkitValues();
    }

    /**
     * Gets the lore of the item.
     *
     * @return The lore of the item.
     */
    public List<Component> getLore() {
        ItemLore loreComponent = item.get(DataComponents.LORE);
        if (loreComponent == null) return List.of();
        return loreComponent.lines();
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The new lore to set.
     */
    public void setLore(List<Component> lore) {
        item.set(DataComponents.LORE, new ItemLore(lore));
    }

    /**
     * Gets the name of the item.
     *
     * @return The name of the item.
     */
    public Component getName() {
        return item.getHoverName();
    }

    /**
     * Sets the name of the item.
     *
     * @param name The new name to set.
     */
    public void setName(Component name) {
        item.set(DataComponents.CUSTOM_NAME, name);
    }

    /**
     * Hides additional information about the item, such as additional tooltip, jukebox playable, fireworks, and attribute modifiers.
     */
    public void hideFlags() {
        item.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, HIDDEN_COMPONENTS_PREVIOUSLY));
    }

    /**
     * Sets the dye color of the item.
     *
     * @param color The new dye color to set.
     */
    public void setDyeColor(int color) {
        item.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param modelData The new custom model data to set.
     */
    public void setCustomModelData(int modelData) {
        item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) modelData), List.of(), List.of(), List.of()));
    }

    /**
     * Sets the profile of the item, for use with player heads.
     *
     * @param uuid      The UUID of the player.
     * @param value     The value of the profile.
     * @param signature The signature of the profile.
     */
    public void setProfile(UUID uuid, String value, String signature) {
        Multimap<String, Property> map = ImmutableMultimap.<String, Property>builder()
                .put("textures", new Property("textures", value, signature))
                .build();
        this.item.set(DataComponents.PROFILE, ResolvableProfile.createResolved(new GameProfile(uuid, value, new PropertyMap(map))));
    }

    /**
     * Sets the profile of the item, for use with player heads, resolved dynamically.
     *
     * @param uuid The UUID of the player.
     */
    public void setProfile(UUID uuid) {
        item.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(uuid));
    }

    /**
     * Sets the profile of the item, for use with player heads, resolved dynamically.
     *
     * @param name The username of the player.
     */
    public void setProfile(String name) {
        item.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(name));
    }

    /**
     * Removes the item's data.
     */
    public void removeItemData() {
        item.remove(DataComponents.CUSTOM_DATA);
        data = null;
    }


    // This method doesn't fit the theme of entire abstraction of item data, but its use case is very specific.

    /**
     * Gets the container of the item.
     *
     * @return The container of the item.
     */
    @Nullable
    public ItemContainerContents getContainer() {
        return item.get(DataComponents.CONTAINER);
    }
}
