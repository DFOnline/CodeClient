package dev.dfonline.codeclient.data;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents an item, with configurable data, lore, name, and more.
 */
public class DFItem {
    ItemStack item;
    ItemData data;

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
    public String getHypercubeStringValue(String key) {
        var itemData = getItemData();
        if (itemData == null) return "";
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
        item.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        item.remove(DataComponentTypes.JUKEBOX_PLAYABLE);
        item.remove(DataComponentTypes.FIREWORKS);
        item.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    }

    /**
     * Sets the dye color of the item.
     *
     * @param color The new dye color to set.
     */
    public void setDyeColor(int color) {
        item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param modelData The new custom model data to set.
     */
    public void setCustomModelData(int modelData) {
        item.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelData));
    }

    /**
     * Sets the profile of the item, for use with player heads.
     *
     * @param uuid      The UUID of the player.
     * @param value     The value of the profile.
     * @param signature The signature of the profile.
     */
    public void setProfile(UUID uuid, String value, String signature) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        item.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
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
