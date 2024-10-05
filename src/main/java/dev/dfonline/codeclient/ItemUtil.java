package dev.dfonline.codeclient;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public class ItemUtil {
    public static Optional<NbtCompound> getPublicBukkitValues(ItemStack stack) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return Optional.empty();
        var nbt = customData.copyNbt();
        if (nbt == null) return Optional.empty();
        return Optional.ofNullable(nbt.getCompound("PublicBukkitValues"));
    }
}
