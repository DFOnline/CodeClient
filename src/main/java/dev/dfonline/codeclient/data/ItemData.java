package dev.dfonline.codeclient.data;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ItemData {
    private NbtCompound customData;

    public ItemData(ItemStack item) {
        var customDataComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent != null) {
            customData = customDataComponent.copyNbt();
        }
    }

    public NbtCompound getCustomData() {
        // Should only be used in very specific cases, the entire point of this class is to abstract the NBT data.
        if (customData == null) {
            customData = new NbtCompound();
        }
        return customData;
    }

    @Nullable
    public PublicBukkitValues getPublicBukkitValues() {
        return PublicBukkitValues.fromItemData(this);
    }

    public void setPublicBukkitValues(PublicBukkitValues publicBukkitValues) {
        getCustomData().put(PublicBukkitValues.PUBLIC_BUKKIT_VALUES_KEY, publicBukkitValues.publicBukkitValues);
    }

    public void setStringValue(String key, String value) {
        customData.putString(key, value);
    }

    public void removeKey(String key) {
        customData.remove(key);
    }

    public String getHypercubeStringValue(String key) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) return "";
        return publicBukkitValues.getHypercubeStringValue(key);
    }

    public void setHypercubeStringValue(String key, String value) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) {
            setPublicBukkitValues(PublicBukkitValues.getEmpty());
            // Should be non-null now.
            publicBukkitValues = getPublicBukkitValues();
        }
        publicBukkitValues.setHypercubeStringValue(key, value);
    }

    public boolean hasHypercubeKey(String key) {
        var publicBukkitValues = getPublicBukkitValues();
        if (publicBukkitValues == null) return false;
        return publicBukkitValues.hasHypercubeKey(key);
    }

    public NbtComponent toComponent() {
        return NbtComponent.of(getCustomData());
    }
}
