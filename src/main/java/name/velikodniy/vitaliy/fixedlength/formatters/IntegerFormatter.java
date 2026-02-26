package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

/**
 * Formatter for {@link Integer} and {@code int} values.
 *
 * <p>Parses using {@link Integer#parseInt(String)} and formats
 * using {@link Integer#toString()}.
 */
public class IntegerFormatter extends Formatter<Integer> {

    /** {@inheritDoc} */
    @Override
    public Integer asObject(String string, FixedField field) {
        return Integer.parseInt(string);
    }

    /** {@inheritDoc} */
    @Override
    public String asString(Integer object, FixedField field) {
        return object.toString();
    }
}
