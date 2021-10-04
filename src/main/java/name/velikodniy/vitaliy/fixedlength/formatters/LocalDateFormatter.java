package name.velikodniy.vitaliy.fixedlength.formatters;

import name.velikodniy.vitaliy.fixedlength.annotation.FixedField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateFormatter extends Formatter<LocalDate> {

    private static final String DEFAULT_FORMAT = "yyyyMMdd";

    private static DateTimeFormatter format(FixedField field) {
        String format;
        if (!field.format().isEmpty()) {
            format = field.format();
        } else {
            format = DEFAULT_FORMAT;
        }
        return DateTimeFormatter.ofPattern(format);
    }

    @Override
    public LocalDate asObject(String string, FixedField field) {
        return LocalDate.parse(string, format(field));
    }

    @Override
    public String asString(LocalDate object, FixedField field) {
        return object.format(format(field));
    }
}
