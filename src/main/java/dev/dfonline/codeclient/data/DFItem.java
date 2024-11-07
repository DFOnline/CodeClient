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

public class DFItem {
    ItemStack item;
    ItemData data;

    public DFItem(ItemStack item) {
        this.item = item;
        this.data = new ItemData(item);
    }

    public static DFItem of(ItemStack item) {
        return new DFItem(item);
    }

    public ItemData getItemData() {
        return data;
    }

    public void setItemData(ItemData itemData) {
        data = itemData;
    }

    public void editData(Consumer<ItemData> consumer) {
        var itemData = getItemData();
        if (itemData == null) return;
        consumer.accept(itemData);
    }

    public void setStringValue(String key, String value) {
        var itemData = getItemData();
        if (itemData == null) return;
        itemData.setStringValue(key, value);
    }

    public String getHypercubeStringValue(String key) {
        var itemData = getItemData();
        if (itemData == null) return "";
        return itemData.getHypercubeStringValue(key);
    }

    public boolean hasHypercubeKey(String key) {
        var itemData = getItemData();
        if (itemData == null) return false;
        return itemData.hasHypercubeKey(key);
    }

    public ItemStack getItemStack() {
        if (data != null) item.set(DataComponentTypes.CUSTOM_DATA, getItemData().toComponent());
        return item;
    }

    public PublicBukkitValues getPublicBukkitValues() {
        return getItemData().getPublicBukkitValues();
    }

    public List<Text> getLore() {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent == null) return List.of();
        return loreComponent.lines();
    }

    public void setLore(List<Text> lore) {
        item.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }

    public Text getName() {
        return item.getName();
    }

    public void setName(Text name) {
        item.set(DataComponentTypes.CUSTOM_NAME, name);
    }

    public void hideFlags() {
        item.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        item.remove(DataComponentTypes.JUKEBOX_PLAYABLE);
        item.remove(DataComponentTypes.FIREWORKS);
        item.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
    }

    public void setDyeColor(int color) {
        item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
    }

    public void setCustomModelData(int modelData) {
        item.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelData));
    }

    public void setProfile(UUID uuid, String value, String signature) {
        PropertyMap map = new PropertyMap();
        map.put("textures", new Property("textures", value, signature));
        item.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.ofNullable(uuid), map));
    }

    public void removeItemData() {
        item.remove(DataComponentTypes.CUSTOM_DATA);
        data = null;
    }

    public void setContainer(ContainerComponent container) {
        item.set(DataComponentTypes.CONTAINER, container);
    }

    @Nullable
    public ContainerComponent getContainer() {
        ContainerComponent containerData = item.get(DataComponentTypes.CONTAINER);
        return containerData;
    }
}
