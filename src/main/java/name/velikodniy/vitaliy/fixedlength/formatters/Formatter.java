package name.velikodniy.vitaliy.fixedlength.formatters;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

/**
 * Abstract base class for type-specific formatters that convert
 * between {@link String} representations in fixed-length fields
 * and Java objects.
 *
 * <p>Built-in formatters are provided for common types:
 * {@link String}, {@link Integer}, {@link Short}, {@link Long},
 * {@link BigDecimal}, {@link Date}, {@link LocalDate},
 * {@link LocalTime}, and {@link LocalDateTime}.
 *
 * <p>Custom formatters can be created by extending this class
 * and registering them via
 * {@link name.velikodniy.vitaliy.fixedlength.FixedLength#registerFormatter}.
 *
 * <p>Implementations must be stateless and thread-safe because
 * formatter instances are cached and shared.
 *
 * @param <T> the Java type this formatter handles
 */
public abstract class Formatter<T> {
    private static final Map<
            Class<? extends Serializable>,
            Class<? extends Formatter<? extends Serializable>>>
            DEFAULT_FORMATTERS = new HashMap<>();

    private static final Map<
            Class<? extends Formatter<?>>,
            Formatter<?>> INSTANCE_CACHE
            = new ConcurrentHashMap<>();

    static {
        DEFAULT_FORMATTERS.put(String.class, StringFormatter.class);
        DEFAULT_FORMATTERS.put(short.class, ShortFormatter.class);
        DEFAULT_FORMATTERS.put(Short.class, ShortFormatter.class);
        DEFAULT_FORMATTERS.put(int.class, IntegerFormatter.class);
        DEFAULT_FORMATTERS.put(Integer.class, IntegerFormatter.class);
        DEFAULT_FORMATTERS.put(long.class, LongFormatter.class);
        DEFAULT_FORMATTERS.put(Long.class, LongFormatter.class);
        DEFAULT_FORMATTERS.put(Date.class, DateFormatter.class);
        DEFAULT_FORMATTERS.put(LocalDate.class, LocalDateFormatter.class);
        DEFAULT_FORMATTERS.put(LocalTime.class, LocalTimeFormatter.class);
        DEFAULT_FORMATTERS.put(LocalDateTime.class, LocalDateTimeFormatter.class);
        DEFAULT_FORMATTERS.put(BigDecimal.class, BigDecimalFormatter.class);
    }

    /**
     * Returns a mutable copy of the default type-to-formatter mapping.
     *
     * <p>Modifications to the returned map do not affect the
     * built-in defaults.
     *
     * @return a new {@link HashMap} containing the default formatters
     */
    public static Map<
            Class<? extends Serializable>,
            Class<? extends Formatter<? extends Serializable>>>
        getDefaultFormatters() {
        return new HashMap<>(DEFAULT_FORMATTERS);
    }

    /**
     * Returns a (cached) formatter instance for the given field type.
     *
     * <p>Formatter instances are expected to be stateless and
     * thread-safe. Instances are created once via reflection and
     * cached for the lifetime of the JVM.
     *
     * @param formatters the type-to-formatter-class mapping
     * @param type       the Java type to find a formatter for
     * @return a formatter instance capable of handling {@code type}
     * @throws FixedLengthException if no formatter is registered
     *                               for {@code type}, or if the
     *                               formatter cannot be instantiated
     */
    public static Formatter<?> instance(
            Map<Class<? extends Serializable>,
                    Class<? extends Formatter<? extends Serializable>>> formatters,
            final Class<?> type
    ) throws FixedLengthException {
        Class<? extends Formatter<?>> formatterClass = formatters.get(type);

        if (formatterClass == null) {
            throw new FixedLengthException(
                    "No formatter found for class " + type.getName()
            );
        }

        return INSTANCE_CACHE.computeIfAbsent(formatterClass, cls -> {
            try {
                return cls.getConstructor().newInstance();
            } catch (Exception e) {
                throw new FixedLengthException(
                        "Cannot create new instance of formatter "
                                + cls.getName()
                );
            }
        });
    }

    /**
     * Parses a string value into an object, returning {@code null}
     * if the input is {@code null}.
     *
     * @param value the raw string from the fixed-length field,
     *              may be {@code null}
     * @param field the field annotation providing format metadata
     * @return the parsed object, or {@code null}
     */
    public T parse(String value, FixedField field) {
        T result = null;
        if (value != null) {
            result = asObject(value, field);
        }
        return result;
    }

    /**
     * Converts a raw string value from a fixed-length field into
     * a typed Java object.
     *
     * @param string the non-null string to parse
     * @param field  the field annotation providing format metadata
     * @return the parsed object
     */
    public abstract T asObject(String string, FixedField field);

    /**
     * Converts a typed Java object into its string representation
     * for a fixed-length field.
     *
     * <p>The {@code object} parameter is guaranteed to be non-null
     * when called through the normal parsing/formatting flow in
     * {@link name.velikodniy.vitaliy.fixedlength.FixedLength}.
     *
     * @param object the non-null object to format
     * @param field  the field annotation providing format metadata
     * @return the string representation
     */
    public abstract String asString(T object, FixedField field);
}
