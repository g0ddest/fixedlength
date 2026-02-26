package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

/**
 * Pass-through formatter for {@link String} values.
 *
 * <p>Returns the input string as-is in both directions.
 */
public class StringFormatter extends Formatter<String> {

    /** {@inheritDoc} */
    @Override
    public String asObject(String string, FixedField field) {
        return string;
    }

    /** {@inheritDoc} */
    @Override
    public String asString(String object, FixedField field) {
        return object;
    }
}
