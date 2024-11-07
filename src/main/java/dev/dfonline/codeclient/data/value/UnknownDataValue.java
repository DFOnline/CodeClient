package dev.dfonline.codeclient.data.value;

import dev.dfonline.codeclient.data.DataType;

public class UnknownDataValue extends DataValue {
    public UnknownDataValue(Object value) {
        super(DataType.UNKNOWN, value);
    }
}
