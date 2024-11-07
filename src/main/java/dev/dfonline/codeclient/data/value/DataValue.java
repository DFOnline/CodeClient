package dev.dfonline.codeclient.data.value;

import dev.dfonline.codeclient.data.DataType;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

public class DataValue {
    DataType type;
    Object value;

    public DataValue(DataType dataType, Object value) {
        this.type = dataType;
        this.value = value;
    }

    public static DataValue fromNbt(NbtElement nbt) {
        if (nbt instanceof NbtString) {
            return new StringDataValue(nbt.asString());
        }
        if (nbt instanceof AbstractNbtNumber) {
            return new NumberDataValue(((AbstractNbtNumber) nbt).doubleValue());
        }
        return new UnknownDataValue(nbt);
    }

    @Nullable
    public Object getValue() {
        return value;
    }
}
