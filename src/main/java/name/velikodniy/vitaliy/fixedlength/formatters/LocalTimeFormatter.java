package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Formatter for {@link LocalTime} values.
 *
 * <p>The format pattern is taken from {@link FixedField#format()}.
 * If not specified, defaults to {@code "HHmmss"}.
 *
 * <p>{@link DateTimeFormatter} instances are thread-safe and
 * immutable, so they are cached in a shared concurrent map.
 */
public class LocalTimeFormatter extends Formatter<LocalTime> {

    private static final String DEFAULT_FORMAT = "HHmmss";

    private static final ConcurrentMap<String, DateTimeFormatter>
            CACHE = new ConcurrentHashMap<>();

    private static DateTimeFormatter format(FixedField field) {
        String pattern = !field.format().isEmpty()
                ? field.format() : DEFAULT_FORMAT;
        return CACHE.computeIfAbsent(
                pattern, DateTimeFormatter::ofPattern);
    }

    /** {@inheritDoc} */
    @Override
    public LocalTime asObject(
            String string, FixedField field) {
        return LocalTime.parse(string, format(field));
    }

    /** {@inheritDoc} */
    @Override
    public String asString(
            LocalTime object, FixedField field) {
        return object.format(format(field));
    }
}
