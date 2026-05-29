package dev.dfonline.codeclient.data;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

    private static final SequencedSet<ComponentType<?>> HIDDEN_COMPONENTS_PREVIOUSLY = ReferenceLinkedOpenHashSet.of(
            DataComponentTypes.BANNER_PATTERNS,
            DataComponentTypes.BEES,
            DataComponentTypes.BLOCK_ENTITY_DATA,
            DataComponentTypes.BLOCK_STATE,
            DataComponentTypes.BUNDLE_CONTENTS,
            DataComponentTypes.CHARGED_PROJECTILES,
            DataComponentTypes.CONTAINER,
            DataComponentTypes.CONTAINER_LOOT,
            DataComponentTypes.FIREWORK_EXPLOSION,
            DataComponentTypes.FIREWORKS,
            DataComponentTypes.INSTRUMENT,
            DataComponentTypes.JUKEBOX_PLAYABLE,
            DataComponentTypes.MAP_ID,
            DataComponentTypes.PAINTING_VARIANT,
            DataComponentTypes.POT_DECORATIONS,
            DataComponentTypes.POTION_CONTENTS,
            DataComponentTypes.TROPICAL_FISH_PATTERN,
            DataComponentTypes.WRITTEN_BOOK_CONTENT
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
        if (data != null) item.set(DataComponentTypes.CUSTOM_DATA, getItemData().toComponent());
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
    public List<Text> getLore() {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent == null) return List.of();
        return loreComponent.lines();
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The new lore to set.
     */
    public void setLore(List<Text> lore) {
        item.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }

    /**
     * Gets the name of the item.
     *
     * @return The name of the item.
     */
    public Text getName() {
        return item.getName();
    }

    /**
     * Sets the name of the item.
     *
     * @param name The new name to set.
     */
    public void setName(Text name) {
        item.set(DataComponentTypes.CUSTOM_NAME, name);
    }

    /**
     * Hides additional information about the item, such as additional tooltip, jukebox playable, fireworks, and attribute modifiers.
     */
    public void hideFlags() {
        item.set(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(false, HIDDEN_COMPONENTS_PREVIOUSLY));
    }

    /**
     * Sets the dye color of the item.
     *
     * @param color The new dye color to set.
     */
    public void setDyeColor(int color) {
        item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param modelData The new custom model data to set.
     */
    public void setCustomModelData(int modelData) {
        item.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of((float) modelData), List.of(), List.of(), List.of()));
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
        this.item.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(new GameProfile(uuid, value, new PropertyMap(map))));
    }

    /**
     * Sets the profile of the item, for use with player heads, resolved dynamically.
     *
     * @param uuid The UUID of the player.
     */
    public void setProfile(UUID uuid) {
        item.set(DataComponentTypes.PROFILE, ProfileComponent.ofDynamic(uuid));
    }

    /**
     * Sets the profile of the item, for use with player heads, resolved dynamically.
     *
     * @param name The username of the player.
     */
    public void setProfile(String name) {
        item.set(DataComponentTypes.PROFILE, ProfileComponent.ofDynamic(name));
    }

    /**
     * Removes the item's data.
     */
    public void removeItemData() {
        item.remove(DataComponentTypes.CUSTOM_DATA);
        data = null;
    }


    // This method doesn't fit the theme of entire abstraction of item data, but its use case is very specific.

    /**
     * Gets the container of the item.
     *
     * @return The container of the item.
     */
    @Nullable
    public ContainerComponent getContainer() {
        return item.get(DataComponentTypes.CONTAINER);
    }
}
