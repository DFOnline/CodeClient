package dev.dfonline.codeclient.data.value;

import dev.dfonline.codeclient.data.DataType;

public class NumberDataValue extends DataValue {
    public NumberDataValue(double value) {
        super(DataType.NUMBER, value);
    }

    public Double getValue() {
        return (Double) super.getValue();
    }
}
