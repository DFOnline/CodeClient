package dev.dfonline.codeclient.data.value;

/**
 * Represents a value of a key that is a number.
 */
public class NumberDataValue extends DataValue {
    public NumberDataValue(double value) {
        super(value);
    }

    public Double getValue() {
        return (Double) super.getValue();
    }
}
