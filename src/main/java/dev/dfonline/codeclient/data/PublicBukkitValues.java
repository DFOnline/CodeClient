package dev.dfonline.codeclient.data;

import dev.dfonline.codeclient.data.value.DataValue;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class PublicBukkitValues {
    public static final String PUBLIC_BUKKIT_VALUES_KEY = "PublicBukkitValues";
    private static final String HYPERCUBE_KEY_PREFIX = "hypercube:";

    NbtCompound publicBukkitValues;

    private PublicBukkitValues(NbtCompound publicBukkitValues) {
        this.publicBukkitValues = publicBukkitValues;
    }

    @Nullable
    public static PublicBukkitValues fromItemData(ItemData data) {
        var customData = data.getCustomData();
        if (customData == null) return null;
        var publicBukkitValues = customData.getCompound(PUBLIC_BUKKIT_VALUES_KEY);
        if (publicBukkitValues == null) return null;
        return new PublicBukkitValues(publicBukkitValues);
    }

    public static PublicBukkitValues getEmpty() {
        var empty = new NbtCompound();
        empty.put(PUBLIC_BUKKIT_VALUES_KEY, new NbtCompound());
        return new PublicBukkitValues(empty);
    }

    public String getHypercubeStringValue(String key) {
        return publicBukkitValues.getString(HYPERCUBE_KEY_PREFIX + key);
    }

    public String getStringValue(String key) {
        return publicBukkitValues.getString(key);
    }

    public DataValue getHypercubeValue(String key) {
        return DataValue.fromNbt(publicBukkitValues.get(HYPERCUBE_KEY_PREFIX + key));
    }

    public Set<String> getHypercubeKeys() {
        return publicBukkitValues.getKeys().stream()
                .filter(key -> key.startsWith(HYPERCUBE_KEY_PREFIX))
                .map(key -> key.substring(10))
                .collect(Collectors.toSet());
    }

    public Set<String> getKeys() {
        return publicBukkitValues.getKeys();
    }

    public boolean hasHypercubeKey(String key) {
        return publicBukkitValues.contains(HYPERCUBE_KEY_PREFIX + key);
    }

    public boolean hasKey(String key) {
        return publicBukkitValues.contains(key);
    }

    public void setStringValue(String key, String value) {
        publicBukkitValues.putString(key, value);
    }

    public void setHypercubeStringValue(String key, String value) {
        publicBukkitValues.putString(HYPERCUBE_KEY_PREFIX + key, value);
    }
}
