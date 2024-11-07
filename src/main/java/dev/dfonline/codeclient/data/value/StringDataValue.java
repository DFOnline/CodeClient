package dev.dfonline.codeclient.data.value;

import dev.dfonline.codeclient.data.DataType;

public class StringDataValue extends DataValue {
    public StringDataValue(String value) {
        super(DataType.STRING, value);
    }

    public String getValue() {
        return (String) super.getValue();
    }
}
