package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formatter for {@link Date} values using {@link SimpleDateFormat}.
 *
 * <p>The format pattern is taken from {@link FixedField#format()}.
 * If not specified, defaults to {@code "yyyyMMdd"}.
 *
 * <p>Since {@link SimpleDateFormat} is not thread-safe, a new
 * instance is created for each operation.
 */
public class DateFormatter extends Formatter<Date> {

    private static final String DEFAULT_FORMAT = "yyyyMMdd";

    private static SimpleDateFormat format(FixedField field) {
        String pattern = !field.format().isEmpty()
                ? field.format() : DEFAULT_FORMAT;
        return new SimpleDateFormat(pattern);
    }

    /** {@inheritDoc} */
    @Override
    public Date asObject(String string, FixedField field) {
        try {
            return format(field).parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String asString(Date object, FixedField field) {
        return format(field).format(object);
    }
}
