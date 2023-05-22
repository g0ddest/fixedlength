package name.velikodniy.vitaliy.fixedlength.formatters;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

public abstract class Formatter<T> {
    private static final Map<Class<? extends Serializable>, Class<? extends Formatter<? extends Serializable>>>
            DEFAULT_FORMATTERS = new HashMap<>();

    public static Map<Class<? extends Serializable>, Class<? extends Formatter<? extends Serializable>>>
        getDefaultFormatters() {
        return DEFAULT_FORMATTERS;
    }

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

    public static Formatter<?> instance(
            Map<Class<? extends Serializable>,
                    Class<? extends Formatter<? extends Serializable>>> formatters, final Class<?> type
    ) throws FixedLengthException {
        Class<? extends Formatter<?>> formatterClass = formatters.get(type);

        if (formatterClass != null) {
            try {
                return formatterClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new FixedLengthException(
                        "Cannot create new instance of formatter " + formatterClass.getName()
                );
            }
        } else {
            throw new FixedLengthException("Not found formatter for class " + type.getName());
        }

    }

    public T parse(String value, FixedField field) {
        T result = null;
        if (value != null) {
            result = asObject(value, field);
        }
        return result;
    }

    public abstract T asObject(String string, FixedField field);

    public abstract String asString(T object, FixedField field);
}
