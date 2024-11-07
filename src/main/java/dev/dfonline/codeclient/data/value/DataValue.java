package dev.dfonline.codeclient.data.value;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a value of a key that can be of a specific type,
 * where recognized types can return the value as their respective data type.
 */
public class DataValue {
    private final Object value;

    /**
     * Creates a new DataValue with the given value.
     *
     * @param value The value of the DataValue.
     */
    protected DataValue(Object value) {
        this.value = value;
    }

    /**
     * Creates a new DataValue with the given value, where if a recognized type is given,
     * a DataType of that type will be created.
     * @param nbt The NbtElement to create the DataValue from.
     * @return The new DataValue.
     */
    public static DataValue fromNbt(NbtElement nbt) {
        if (nbt instanceof NbtString) {
            return new StringDataValue(nbt.asString());
        }
        if (nbt instanceof AbstractNbtNumber) {
            return new NumberDataValue(((AbstractNbtNumber) nbt).doubleValue());
        }
        return new UnknownDataValue(nbt);
    }

    /**
     * Gets the value of the DataValue.
     * @return The value of the DataValue as an Object.
     * @implNote Make sure you really want to use this instead of the classes for specific types.
     */
    @Nullable
    public Object getValue() {
        return value;
    }
}
