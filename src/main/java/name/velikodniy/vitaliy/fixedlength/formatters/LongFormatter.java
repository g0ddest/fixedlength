package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

/**
 * Formatter for {@link Long} and {@code long} values.
 *
 * <p>Parses using {@link Long#parseLong(String)} and formats
 * using {@link Long#toString()}.
 */
public class LongFormatter extends Formatter<Long> {

    /** {@inheritDoc} */
    @Override
    public Long asObject(String string, FixedField field) {
        return Long.parseLong(string);
    }

    /** {@inheritDoc} */
    @Override
    public String asString(Long object, FixedField field) {
        return object.toString();
    }
}
