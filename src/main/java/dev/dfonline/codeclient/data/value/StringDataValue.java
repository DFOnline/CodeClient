package dev.dfonline.codeclient.data.value;

/**
 * Represents a value of a key that is a string.
 */
public class StringDataValue extends DataValue {
    public StringDataValue(String value) {
        super(value);
    }

    public String getValue() {
        return (String) super.getValue();
    }
}
