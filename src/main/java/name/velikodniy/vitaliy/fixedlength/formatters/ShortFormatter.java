package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

/**
 * Formatter for {@link Short} and {@code short} values.
 *
 * <p>Parses using {@link Short#parseShort(String)} and formats
 * using {@link Short#toString()}.
 */
public class ShortFormatter extends Formatter<Short> {

    /** {@inheritDoc} */
    @Override
    public Short asObject(String string, FixedField field) {
        return Short.parseShort(string);
    }

    /** {@inheritDoc} */
    @Override
    public String asString(Short object, FixedField field) {
        return object.toString();
    }
}
