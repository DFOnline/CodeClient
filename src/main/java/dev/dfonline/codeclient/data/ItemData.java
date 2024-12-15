package dev.dfonline.codeclient.data;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ItemData {
    private NbtCompound customData;
    private PublicBukkitValues publicBukkitValues;

    /**
     * Creates a new ItemData from an ItemStack.
     *
     * @param item The item to create the ItemData from.
     * @implNote Most operations won't work if the item doesn't have custom data.
     */
    public ItemData(ItemStack item) {
        var customDataComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent != null) {
            customData = customDataComponent.copyNbt();
        }
    }

    /**
     * Creates an ItemData with an existing empty NbtCompound.
     */
    private ItemData() {
        this.customData = new NbtCompound();
    }

    /**
     * Gets the NBT Compound of the CUSTOM_DATA item component, applying the PublicBukkitValues along the way.
     *
     * @return The NBT Compound of the CUSTOM_DATA item component.
     * @apiNote This should only be used in very specific cases, the entire point of this class is to abstract the NBT data.
     */
    public NbtCompound getNbt() {
        // Should only be used in very specific cases, the entire point of this class is to abstract the NBT data.
        if (customData == null) {
            customData = new NbtCompound();
        }
        if (publicBukkitValues != null)
            customData.put(PublicBukkitValues.PUBLIC_BUKKIT_VALUES_KEY, publicBukkitValues.getNbt());
        return customData;
    }

    /**
     * Creates an empty ItemData.
     */
    public static ItemData getEmpty() {
        return new ItemData();
    }

    /**
     * Checks if the item data has custom data.
     *
     * @return Whether the item data has custom data.
     */
    public boolean hasCustomData() {
        return customData != null;
    }

    /**
     * Gets the PublicBukkitValues from the item data.
     *
     * @return The PublicBukkitValues.
     */
    @Nullable
    public PublicBukkitValues getPublicBukkitValues() {
        if (publicBukkitValues == null) {
            publicBukkitValues = PublicBukkitValues.fromItemData(this);
        }
        return publicBukkitValues;
    }

    /**
     * Sets the PublicBukkitValues of the item data.
     *
     * @param publicBukkitValues The PublicBukkitValues to set.
     */
    public void setPublicBukkitValues(PublicBukkitValues publicBukkitValues) {
        this.publicBukkitValues = publicBukkitValues;
    }

    /**
     * Removes a key from the custom data.
     *
     * @param key The key to remove.
     */
    public void removeKey(String key) {
        customData.remove(key);
    }

    /**
     * Gets a String value of a key.
     *
     * @param key The key to get.
     * @return The value of the key, or an empty string if it doesn't exist.
     */
    public String getStringValue(String key) {
        return customData.getString(key);
    }

    /**
     * Gets a String value of a key.
     *
     * @param key   The key to get.
     * @param value The value of the key.
     */
    public void setStringValue(String key, String value) {
        customData.putString(key, value);
    }

    /**
     * Checks if the custom data has a key.
     *
     * @param key The key to check.
     * @return Whether the custom data has the key.
     */
    public boolean hasKey(String key) {
        return customData.contains(key);
    }

    /**
     * Delegates to {@link PublicBukkitValues#getHypercubeStringValue(String)}.
     *
     * @param key The key to get, without the hypercube: prefix.
     * @return The value of the key, or an empty string if it doesn't exist.
     */
    public String getHypercubeStringValue(String key) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) return "";
        return publicBukkitValues.getHypercubeStringValue(key);
    }

    /**
     * Delegates to {@link PublicBukkitValues#setHypercubeStringValue(String, String)}.
     *
     * @param key   The key to set, without the hypercube: prefix.
     * @param value The value to set.
     */
    public void setHypercubeStringValue(String key, String value) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) {
            publicBukkitValues = PublicBukkitValues.getEmpty();
        }
        publicBukkitValues.setHypercubeStringValue(key, value);
    }

    /**
     * Delegates to {@link PublicBukkitValues#hasHypercubeKey(String)}.
     *
     * @param key The key to check, without the hypercube: prefix.
     * @return Whether the key exists.
     */
    public boolean hasHypercubeKey(String key) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) return false;
        return publicBukkitValues.hasHypercubeKey(key);
    }

    /**
     * Converts the item data to a NbtComponent.
     *
     * @return The NbtComponent.
     * @apiNote This should only be used in very specific cases, the entire point of this class is to abstract the NBT data.
     */
    public NbtComponent toComponent() {
        return NbtComponent.of(getNbt());
    }
}
