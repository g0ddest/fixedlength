package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public abstract class Formatter<T> {
    private static Map<Class<? extends Serializable>, Class<? extends Formatter>> defaultFormatters = new HashMap<>();

    public static Map<Class<? extends Serializable>, Class<? extends Formatter>> getDefaultFormatters() {
        return defaultFormatters;
    }

    static {
        defaultFormatters.put(String.class, StringFormatter.class);
        defaultFormatters.put(short.class, ShortFormatter.class);
        defaultFormatters.put(Short.class, ShortFormatter.class);
        defaultFormatters.put(int.class, IntegerFormatter.class);
        defaultFormatters.put(Integer.class, IntegerFormatter.class);
        defaultFormatters.put(long.class, LongFormatter.class);
        defaultFormatters.put(Long.class, LongFormatter.class);
        defaultFormatters.put(Date.class, DateFormatter.class);
        defaultFormatters.put(LocalDate.class, LocalDateFormatter.class);
        defaultFormatters.put(LocalTime.class, LocalTimeFormatter.class);
        defaultFormatters.put(LocalDateTime.class, LocalDateTimeFormatter.class);
        defaultFormatters.put(BigDecimal.class, BigDecimalFormatter.class);
    }

    public static Formatter instance(
            Map<Class<? extends Serializable>,
                    Class<? extends Formatter>> formatters, final Class<?> type
    ) throws FixedLengthException {
        Class<? extends Formatter> formatterClass = formatters.get(type);

        if (formatterClass != null) {
            try {
                return formatterClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new FixedLengthException("Cannot create new instance of formatter " + formatterClass.getName());
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
}
